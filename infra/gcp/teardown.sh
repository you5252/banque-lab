#!/usr/bin/env bash
# =====================================================================
#  Supprime TOUTE l'infrastructure BanqueLab sur Google Cloud (fin des 3 mois).
#  ⚠️ Irréversible : cluster, bases de données de dev/recette/prod, Jenkins et son historique, images.
#      bash infra/gcp/teardown.sh
# =====================================================================
set -euo pipefail
cd "$(dirname "$0")/../.."
source infra/gcp/config.sh
gcloud config set project "$PROJECT_ID" >/dev/null

read -r -p "Tout supprimer dans le projet $PROJECT_ID ? Tape le nom du projet pour confirmer : " answer
[[ "$answer" == "$PROJECT_ID" ]] || { echo "Annulé."; exit 1; }

gcloud container clusters delete "$CLUSTER" --zone "$ZONE" --quiet || true
gcloud compute instances delete "$JENKINS_VM" --zone "$ZONE" --quiet || true
gcloud compute addresses delete jenkins-ip --region "$REGION" --quiet || true
gcloud compute firewall-rules delete allow-jenkins --quiet || true
gcloud artifacts repositories delete "$AR_REPO" --location "$REGION" --quiet || true
# Disques des bases PostgreSQL (PersistentVolumes) restés orphelins après la suppression du cluster
for d in $(gcloud compute disks list --filter "zone:$ZONE AND name~^pvc-" --format 'value(name)'); do
  gcloud compute disks delete "$d" --zone "$ZONE" --quiet || true
done
echo "✅ Tout est supprimé. Les comptes de service restent (gratuits)."
