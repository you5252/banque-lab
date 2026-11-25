#!/bin/bash
# =====================================================================
#  Script de démarrage de la VM Jenkins (exécuté par Google à CHAQUE démarrage de la VM).
#  Il installe Docker, construit l'image Jenkins (infra/jenkins/Dockerfile) et la lance.
#  Les fichiers de configuration arrivent par les "métadonnées" de la VM (voir setup.sh).
#  Logs : sudo journalctl -u google-startup-scripts.service
# =====================================================================
set -euo pipefail

MD="http://metadata.google.internal/computeMetadata/v1"
md() { curl -fsS -H "Metadata-Flavor: Google" "$MD/$1"; }

# --- 1. Docker (dépôt officiel Docker, une seule fois) ---
if ! command -v docker >/dev/null; then
  apt-get update
  apt-get install -y ca-certificates curl gnupg
  install -m 0755 -d /etc/apt/keyrings
  curl -fsSL https://download.docker.com/linux/debian/gpg -o /etc/apt/keyrings/docker.asc
  echo "deb [signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/debian $(. /etc/os-release && echo "$VERSION_CODENAME") stable" \
    > /etc/apt/sources.list.d/docker.list
  apt-get update
  apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin
fi

# --- 2. Fichiers de configuration Jenkins ---
mkdir -p /opt/jenkins/casc
cd /opt/jenkins
md instance/attributes/jenkins-dockerfile > Dockerfile
md instance/attributes/jenkins-plugins    > plugins.txt
md instance/attributes/jenkins-casc       > casc/casc.yaml

# Mot de passe admin généré une seule fois (lisible seulement par root)
if [ ! -s admin-password ]; then
  (umask 077; openssl rand -base64 18 | tr -d '/+=' > admin-password)
fi

PROJECT_ID=$(md project/project-id)
EXTERNAL_IP=$(md instance/network-interfaces/0/access-configs/0/external-ip)

# --- 3. Image Jenkins + conteneur ---
docker build -t banque-jenkins /opt/jenkins
docker volume create jenkins_home >/dev/null
docker rm -f jenkins >/dev/null 2>&1 || true

docker run -d --name jenkins --restart unless-stopped \
  -p 8080:8080 \
  -v jenkins_home:/var/jenkins_home \
  -v /var/run/docker.sock:/var/run/docker.sock \
  -v /opt/jenkins/casc:/opt/casc:ro \
  --group-add "$(stat -c %g /var/run/docker.sock)" \
  -e CASC_JENKINS_CONFIG=/opt/casc/casc.yaml \
  -e JAVA_OPTS="-Djenkins.install.runSetupWizard=false -Xmx1g" \
  -e JENKINS_ADMIN_PASSWORD="$(cat admin-password)" \
  -e JENKINS_URL="http://${EXTERNAL_IP}:8080/" \
  -e GCP_PROJECT="$PROJECT_ID" \
  -e GKE_CLUSTER="$(md instance/attributes/gke-cluster)" \
  -e GKE_ZONE="$(md instance/attributes/gke-zone)" \
  -e REGISTRY="$(md instance/attributes/registry)" \
  -e GITHUB_REPO="$(md instance/attributes/github-repo)" \
  banque-jenkins

# Ménage hebdomadaire des images Docker de plus de 7 jours (le disque fait 50 Go)
echo '0 3 * * 0 root docker system prune -af --filter "until=168h" >/dev/null 2>&1' > /etc/cron.d/docker-prune
