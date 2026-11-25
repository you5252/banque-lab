# Image Jenkins de BanqueLab : Jenkins LTS (Java 21) + les outils dont les pipelines ont besoin.
#   - docker CLI   : construire et pousser les images (le moteur Docker est celui de la VM, via docker.sock)
#   - gcloud       : s'authentifier auprès d'Artifact Registry et de GKE avec le compte de service de la VM
#   - kubectl      : déployer dans Kubernetes
# Maven est installé automatiquement par Jenkins (outil "maven3", voir casc.yaml).
FROM jenkins/jenkins:lts-jdk21

USER root
RUN apt-get update \
 && apt-get install -y --no-install-recommends ca-certificates curl gnupg \
 && install -m 0755 -d /etc/apt/keyrings \
 && curl -fsSL https://download.docker.com/linux/debian/gpg -o /etc/apt/keyrings/docker.asc \
 && echo "deb [signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/debian $(. /etc/os-release && echo "$VERSION_CODENAME") stable" \
      > /etc/apt/sources.list.d/docker.list \
 && curl -fsSL https://packages.cloud.google.com/apt/doc/apt-key.gpg | gpg --dearmor -o /etc/apt/keyrings/cloud.google.gpg \
 && echo "deb [signed-by=/etc/apt/keyrings/cloud.google.gpg] https://packages.cloud.google.com/apt cloud-sdk main" \
      > /etc/apt/sources.list.d/google-cloud-sdk.list \
 && apt-get update \
 && apt-get install -y --no-install-recommends \
      docker-ce-cli docker-buildx-plugin \
      google-cloud-cli google-cloud-cli-gke-gcloud-auth-plugin kubectl \
 && rm -rf /var/lib/apt/lists/*
USER jenkins

COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN jenkins-plugin-cli --plugin-file /usr/share/jenkins/ref/plugins.txt
