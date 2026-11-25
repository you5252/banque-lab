#!/usr/bin/env bash
# =====================================================================
#  Crée toute l'infrastructure BanqueLab sur Google Cloud.
#  À lancer UNE fois depuis Cloud Shell, à la racine du dépôt :
#      bash infra/gcp/setup.sh
#  Le script est "idempotent" : on peut le relancer sans danger, il saute ce qui existe déjà.
#
#  Ce qui est créé :
#   - Artifact Registry  : stocke les images Docker construites par la CI
#   - Cluster GKE zonal  : 1 nœud e2-standard-2 "spot" (jusqu'à 2 si besoin) → les environnements dev/recette/prod
#   - VM "jenkins"       : e2-standard-2, Jenkins dans Docker, configuré automatiquement (Configuration as Code)
#   - 2 comptes de service avec le minimum de droits (Jenkins, nœuds GKE)
#   - Un pare-feu qui n'ouvre Jenkins qu'à vos adresses IP
# =====================================================================
set -euo pipefail
cd "$(dirname "$0")/../.."          # racine du dépôt
source infra/gcp/config.sh

[[ "$PROJECT_ID" == "A_REMPLACER" || "$ALLOWED_IPS" == "A_REMPLACER" ]] && {
  echo "❌ Renseigne PROJECT_ID et ALLOWED_IPS dans infra/gcp/config.sh"; exit 1; }

step() { echo; echo "▶ $*"; }
exists() { "$@" >/dev/null 2>&1; }

gcloud config set project "$PROJECT_ID" >/dev/null

step "Activation des API (2-3 min la première fois)"
gcloud services enable compute.googleapis.com container.googleapis.com artifactregistry.googleapis.com

step "Artifact Registry : dépôt d'images $AR_REPO"
exists gcloud artifacts repositories describe "$AR_REPO" --location "$REGION" || \
  gcloud artifacts repositories create "$AR_REPO" --repository-format=docker --location "$REGION" \
    --description "Images BanqueLab construites par Jenkins"
# Ne garder que les 10 dernières versions de chaque image (le stockage au-delà de 0,5 Go est payant)
gcloud artifacts repositories set-cleanup-policies "$AR_REPO" --location "$REGION" \
  --policy infra/gcp/ar-cleanup-policy.json --no-dry-run >/dev/null

step "Comptes de service (principe du moindre privilège)"
exists gcloud iam service-accounts describe "$NODES_SA" || \
  gcloud iam service-accounts create gke-nodes-sa --display-name "Nœuds GKE BanqueLab"
exists gcloud iam service-accounts describe "$JENKINS_SA" || \
  gcloud iam service-accounts create jenkins-sa --display-name "Jenkins BanqueLab"
sleep 10   # laisse le temps aux comptes d'être visibles partout

grant() { gcloud projects add-iam-policy-binding "$PROJECT_ID" --member "serviceAccount:$1" --role "$2" \
            --condition None --quiet >/dev/null; }
# Les nœuds peuvent télécharger les images et écrire leurs logs
for role in roles/artifactregistry.reader roles/logging.logWriter roles/monitoring.metricWriter \
            roles/monitoring.viewer roles/stackdriver.resourceMetadata.writer; do
  grant "$NODES_SA" "$role"
done
# Jenkins peut pousser des images et déployer dans le cluster (mais pas le supprimer)
grant "$JENKINS_SA" roles/artifactregistry.writer
grant "$JENKINS_SA" roles/container.developer

step "Cluster GKE $CLUSTER (5-10 min)"
exists gcloud container clusters describe "$CLUSTER" --zone "$ZONE" || \
  gcloud container clusters create "$CLUSTER" \
    --zone "$ZONE" \
    --release-channel regular \
    --machine-type e2-standard-2 \
    --spot \
    --num-nodes 1 --enable-autoscaling --min-nodes 1 --max-nodes 2 \
    --disk-type pd-standard --disk-size 30 \
    --service-account "$NODES_SA" \
    --logging SYSTEM --monitoring SYSTEM

step "VM Jenkins"
exists gcloud compute addresses describe jenkins-ip --region "$REGION" || \
  gcloud compute addresses create jenkins-ip --region "$REGION"
JENKINS_IP=$(gcloud compute addresses describe jenkins-ip --region "$REGION" --format 'value(address)')

METADATA_FILES="startup-script=infra/gcp/jenkins-startup.sh,jenkins-dockerfile=infra/jenkins/Dockerfile,jenkins-plugins=infra/jenkins/plugins.txt,jenkins-casc=infra/jenkins/casc.yaml"
METADATA="gke-cluster=$CLUSTER,gke-zone=$ZONE,registry=$REGISTRY,github-repo=$GITHUB_REPO"

if exists gcloud compute instances describe "$JENKINS_VM" --zone "$ZONE"; then
  echo "La VM existe : mise à jour de sa configuration (appliquée au prochain redémarrage)"
  gcloud compute instances add-metadata "$JENKINS_VM" --zone "$ZONE" \
    --metadata-from-file "$METADATA_FILES" --metadata "$METADATA"
else
  gcloud compute instances create "$JENKINS_VM" \
    --zone "$ZONE" \
    --machine-type e2-standard-2 \
    --image-family debian-12 --image-project debian-cloud \
    --boot-disk-size 50GB --boot-disk-type pd-balanced \
    --address "$JENKINS_IP" \
    --service-account "$JENKINS_SA" --scopes cloud-platform \
    --tags jenkins \
    --metadata-from-file "$METADATA_FILES" --metadata "$METADATA"
fi

step "Pare-feu : Jenkins (port 8080) accessible uniquement depuis $ALLOWED_IPS"
if exists gcloud compute firewall-rules describe allow-jenkins; then
  gcloud compute firewall-rules update allow-jenkins --source-ranges "$ALLOWED_IPS"
else
  gcloud compute firewall-rules create allow-jenkins --network default --direction INGRESS \
    --allow tcp:8080 --target-tags jenkins --source-ranges "$ALLOWED_IPS"
fi

cat <<EOF

✅ Infrastructure prête.

  Jenkins       : http://$JENKINS_IP:8080   (disponible dans ~5 min, le temps que la VM s'installe)
  Utilisateur   : admin
  Mot de passe  : gcloud compute ssh $JENKINS_VM --zone $ZONE --command 'sudo cat /opt/jenkins/admin-password'
  Images Docker : $REGISTRY

Étape suivante : docs/GUIDE-DEPLOIEMENT-GCP.md, partie A, étape 5 (ajouter le token GitHub dans Jenkins).
EOF
