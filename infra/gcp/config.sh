# =====================================================================
#  Configuration commune aux scripts infra/gcp/*.sh
#  Seules les 2 premières lignes sont à adapter.
# =====================================================================

# ID du projet Google Cloud (visible en haut de la console, ex. banque-lab-123456)
PROJECT_ID="${PROJECT_ID:-A_REMPLACER}"

# Adresses IP autorisées à ouvrir Jenkins (la tienne et celle de ta sœur), séparées par des virgules.
# Trouver son IP : https://ifconfig.me  → puis ajouter /32 à la fin. Ex. "88.12.34.56/32,92.1.2.3/32"
ALLOWED_IPS="${ALLOWED_IPS:-A_REMPLACER}"

# Dépôt GitHub lu par Jenkins
GITHUB_REPO="https://github.com/you5252/banque-lab.git"

# Région Belgique : la moins chère des régions européennes proches
REGION="europe-west1"
ZONE="europe-west1-b"          # cluster ZONAL = gratuit (free tier GKE), un cluster régional serait payant

CLUSTER="banque-lab"
AR_REPO="banque-lab"           # dépôt d'images Docker dans Artifact Registry
JENKINS_VM="jenkins"
REGISTRY="${REGION}-docker.pkg.dev/${PROJECT_ID}/${AR_REPO}"

JENKINS_SA="jenkins-sa@${PROJECT_ID}.iam.gserviceaccount.com"
NODES_SA="gke-nodes-sa@${PROJECT_ID}.iam.gserviceaccount.com"
