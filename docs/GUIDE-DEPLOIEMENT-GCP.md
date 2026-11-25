# ☁️ Déployer BanqueLab sur Google Cloud (3 mois gratuits)

Ce guide remplace Docker Desktop par un vrai environnement cloud : **Jenkins** construit et teste le code, puis **déploie sur Kubernetes (GKE)** dans trois environnements, `dev`, `recette` et `prod`. C'est exactement le schéma qu'on retrouve en banque et en ESN.

- **Partie A** : la mise en place, faite **une seule fois** par la dev (≈ 45 min).
- **Partie B** : le guide de **la testeuse** pour apprendre à déployer.

## 🗺️ Vue d'ensemble

```
 GitHub (main)          VM "jenkins"                        Artifact Registry          GKE (Kubernetes)
 ─────────────          ───────────────────────────         ─────────────────          ──────────────────────────
  push / merge  ──▶   banque-lab-ci                     ──▶  account-service:sha  ──▶  banque-dev      (auto)
  (vu toutes           compile + tests (JUnit, Cucumber)      batch-service:sha        banque-recette  (manuel)
   les 5 min)          construit + pousse les images                                   banque-prod     (manuel + validation)
                       ↓ sur main                                                      banque-kafka    (Kafka partagé)
                      banque-lab-cd  ─────── kubectl apply -k ──────────────────────▶
                       smoke test, rollback automatique si échec
```

| Élément | Où | Pourquoi |
|---|---|---|
| `ci/Jenkinsfile.ci` | Jenkins | CI : chaque branche est compilée et testée ; sur `main` les images sont poussées et la CD dev est lancée |
| `ci/Jenkinsfile.cd` | Jenkins | CD : déploie un tag d'image dans un environnement, avec smoke test et rollback |
| `k8s/base` + `k8s/overlays/{dev,recette,prod}` | GKE | Manifestes Kubernetes ; chaque overlay ajuste le nombre d'instances |
| `k8s/platform/kafka.yaml` | GKE | Un broker Kafka partagé (à remplacer par Strimzi en S8-3) |
| `infra/gcp/*.sh` | Cloud Shell | Création / suppression de toute l'infrastructure |
| `infra/jenkins/*` | VM Jenkins | Image Jenkins + configuration automatique (aucun clic d'installation) |

## 💶 Budget

Le compte d'essai Google Cloud offre **300 $ de crédit valables 90 jours**. Tant que tu ne passes pas en compte payant, **tu ne peux pas être débitée**. À la fin des 90 jours (ou si le crédit est épuisé), tout s'arrête, puis est supprimé après 30 jours.

Estimation (approximative, région Belgique) :

| Ressource | ≈ par mois |
|---|---|
| Frais de gestion du cluster GKE zonal | 0 $ (couvert par le free tier GKE) |
| Nœud GKE e2-standard-2 **spot** (un 2ᵉ s'ajoute seulement si besoin) | 15 à 35 $ |
| VM Jenkins e2-standard-2 allumée 24 h/24 | ≈ 55 $ |
| Disques, IP fixe, images | ≈ 10 $ |
| **Total** | **≈ 80 à 100 $** → ~250-300 $ sur 3 mois |

👉 **Pour garder de la marge, éteins la VM Jenkins quand vous ne travaillez pas** (voir « Économiser » plus bas) : ça divise son coût par 2 ou 3.

> 💡 *Spot* = machine à prix réduit que Google peut reprendre à tout moment. Kubernetes recrée alors les pods sur un autre nœud en quelques minutes. Pour un projet d'apprentissage, c'est parfait (et c'est un bon exercice d'observation).

---

## Partie A · Mise en place (la dev, une seule fois)

### 1. Créer le compte Google Cloud

1. Va sur https://cloud.google.com/free → **Commencer gratuitement**, avec ton compte Google.
2. Renseigne ta carte bancaire (vérification d'identité seulement, pas de débit pendant l'essai).
3. Dans la console, crée un projet : en haut à gauche → **Nouveau projet** → nom `banque-lab`. Note l'**ID du projet** affiché (ex. `banque-lab-471234`).

### 2. Mettre une alerte de budget (2 min, à ne pas sauter)

Console → **Facturation** → **Budgets et alertes** → **Créer un budget** :
- Montant : 300 $ (ou l'équivalent en €)
- Alertes à 25 %, 50 %, 75 % et 90 % → tu reçois un e-mail à chaque palier.

### 3. Ouvrir Cloud Shell et récupérer le dépôt

Cloud Shell est un terminal Linux dans le navigateur, avec `gcloud`, `kubectl` et `git` déjà installés : **rien à installer sur ton PC**.

Console → icône **>_** en haut à droite. Puis :

```bash
git clone https://github.com/you5252/banque-lab.git
cd banque-lab
```

Le dépôt est privé : à la place du mot de passe, colle un **token GitHub** (GitHub → Settings → Developer settings → Personal access tokens → *Fine-grained* → accès au dépôt `banque-lab`, permission **Contents: Read-only**). Garde ce token, il resservira à l'étape 5.

### 4. Lancer la création de l'infrastructure

Ouvre `infra/gcp/config.sh` (bouton **Ouvrir l'éditeur** de Cloud Shell, ou `nano infra/gcp/config.sh`) et remplis :
- `PROJECT_ID` : l'ID noté à l'étape 1 ;
- `ALLOWED_IPS` : ton IP et celle de ta sœur, chacune suivie de `/32` (chacune trouve la sienne sur https://ifconfig.me).

Puis :

```bash
bash infra/gcp/setup.sh
```

Compte 10 à 15 min. À la fin, le script affiche l'adresse de Jenkins et la commande pour lire le mot de passe `admin`. Attends encore ~5 min que la VM finisse de s'installer.

> Ton IP change (box redémarrée, autre Wi-Fi) et Jenkins ne répond plus ? Mets à jour `ALLOWED_IPS` et relance `bash infra/gcp/setup.sh` : il ne recrée rien, il met seulement le pare-feu à jour.

### 5. Brancher Jenkins sur GitHub

1. Ouvre `http://IP_JENKINS:8080` et connecte-toi avec `admin`.
2. **Administrer Jenkins → Credentials → System → Global credentials → Add Credentials** :
   - Kind : *Username with password*
   - Username : `you5252`
   - Password : le token GitHub de l'étape 3
   - **ID : `github`** (exactement ce nom, les jobs l'attendent)
3. Ouvre le job **banque-lab-ci** → **Scan Multibranch Pipeline Now**. Jenkins trouve les branches et lance la CI sur chacune.

Les jobs, les outils (JDK 21, Maven) et les variables sont déjà configurés par `infra/jenkins/casc.yaml`.

### 6. Premier déploiement

Merge la Pull Request qui contient ce guide sur `main`. Dans les 5 minutes :
1. `banque-lab-ci` sur `main` : build, tests, images poussées ;
2. `banque-lab-cd` démarre tout seul avec `TARGET_ENV=dev` ;
3. le premier déploiement crée PostgreSQL et Kafka, il peut prendre 3 à 5 min.

### 7. Donner accès à ta sœur

**Jenkins** : Administrer Jenkins → **Users** → **Create User** (identifiant et mot de passe pour elle).

**Google Cloud** (pour qu'elle puisse regarder les pods depuis son propre Cloud Shell), dans ton Cloud Shell :

```bash
EMAIL=son.adresse@gmail.com
gcloud projects add-iam-policy-binding "$(gcloud config get project)" --member "user:$EMAIL" --role roles/container.developer
gcloud projects add-iam-policy-binding "$(gcloud config get project)" --member "user:$EMAIL" --role roles/viewer
```

Elle peut lire et déployer dans Kubernetes, mais pas supprimer le cluster ni créer de machines.

### Économiser : éteindre / rallumer Jenkins

```bash
gcloud compute instances stop jenkins --zone europe-west1-b    # le soir
gcloud compute instances start jenkins --zone europe-west1-b   # le matin (Jenkins prêt en ~3 min)
```

L'historique et la configuration sont conservés. Les applications dans GKE continuent de tourner.

### À la fin des 3 mois

```bash
bash infra/gcp/teardown.sh
```

Supprime tout (irréversible). Pense à noter vos mesures dans `docs/journal.md` avant.

---

## Partie B · Apprendre à déployer (la testeuse)

### Les mots à connaître

- **Image** : l'application emballée, prête à tourner. Chaque image a un **tag** = les 7 premiers caractères du commit Git (ex. `a1b2c3d`). Même tag = même code, partout.
- **Environnement** : une copie de l'application avec sa propre base de données. `dev` pour les développeurs, `recette` pour tester avant la mise en production, `prod` pour les « vrais » clients.
- **Namespace** : le compartiment Kubernetes d'un environnement (`banque-dev`, `banque-recette`, `banque-prod`).
- **Pod** : une instance de l'application en train de tourner. La prod en a 2.
- **Rollback** : retour à la version précédente.
- **Smoke test** : un test minimal juste après le déploiement (« l'application répond-elle ? »).

### Le cycle de vie d'une version

1. Une PR est mergée sur `main` → la CI construit l'image `a1b2c3d` → **dev est mis à jour tout seul**.
2. Tu testes sur dev. Si c'est bon, **tu déploies le même tag en recette**.
3. Tu fais ta campagne de tests en recette.
4. Feu vert → **tu déploies le même tag en prod**, avec une validation explicite.

On ne reconstruit jamais l'image entre deux environnements : ce qui part en prod est exactement ce qui a été testé.

### Déployer en recette ou en prod

1. Dans Jenkins, ouvre `banque-lab-ci` → branche **main** → dernier build vert → note le tag affiché dans la console (« Version construite : a1b2c3d »).
2. Ouvre **banque-lab-cd** → **Build with Parameters** :
   - `IMAGE_TAG` : `a1b2c3d`
   - `TARGET_ENV` : `recette` (ou `prod`)
3. **Build**. Suis les étapes : Vérifications → (Validation PROD) → Connexion au cluster → Déploiement → Smoke test.
4. Pour la prod, le pipeline s'arrête et demande *« Déployer a1b2c3d en PRODUCTION ? »* → clique **Oui, déployer**.

Si le pipeline échoue, il revient **automatiquement** à la version précédente. Lis le premier stage en rouge pour comprendre pourquoi.

### Regarder ce qui tourne (Cloud Shell)

Ouvre https://console.cloud.google.com, sélectionne le projet `banque-lab`, puis **>_** (Cloud Shell) :

```bash
gcloud container clusters get-credentials banque-lab --zone europe-west1-b   # une fois
kubectl get pods -n banque-recette                     # les pods et leur état
kubectl logs -n banque-recette deploy/account-service  # les logs de l'application
kubectl describe pod -n banque-recette <nom-du-pod>    # le détail si un pod ne démarre pas
kubectl rollout history -n banque-prod deploy/account-service   # l'historique des versions
```

### Ouvrir le Swagger d'un environnement

```bash
kubectl port-forward -n banque-recette svc/account-service 8081:8081
```

Puis, dans Cloud Shell : bouton **Aperçu sur le Web** (en haut) → **Changer de port** → `8081`. Ajoute `/swagger-ui.html` à l'adresse qui s'ouvre. `Ctrl+C` dans le terminal pour arrêter.

### Exercices

1. **Promotion** : prends le tag déployé en dev, déploie-le en recette, puis en prod. Vérifie avec `kubectl get pods` que la prod a bien 2 pods.
2. **Tag inexistant** : lance la CD avec `IMAGE_TAG=zzzzzzz`. Quel stage échoue, et pourquoi c'est une bonne chose ?
3. **Rollback manuel** : en recette, `kubectl rollout undo -n banque-recette deploy/account-service`, puis `rollout history`. Quelle version tourne maintenant ?
4. **Zéro coupure** : pendant un déploiement en prod, lance dans un autre Cloud Shell `kubectl get pods -n banque-prod -w`. Observe : le nouveau pod démarre **avant** que l'ancien s'arrête (`maxUnavailable: 0`).
5. **Panne** : supprime un pod de prod (`kubectl delete pod -n banque-prod <nom>`). Que fait Kubernetes ? En combien de temps ?
6. **Ta place dans la CI** (tâche S7-3) : ajoute le stage « Tests Playwright » dans `ci/Jenkinsfile.ci`.

Note tes observations dans `docs/journal.md`.

---

## 🧯 Dépannage

| Symptôme | Cause probable | Que faire |
|---|---|---|
| Jenkins ne répond pas | VM éteinte, ou ton IP a changé | `gcloud compute instances start jenkins ...` ; mettre à jour `ALLOWED_IPS` et relancer `setup.sh` |
| Jenkins répond mais pas de jobs | Démarrage pas fini | Attendre 3 min. Logs : `gcloud compute ssh jenkins --zone europe-west1-b --command 'sudo docker logs jenkins --tail 50'` |
| CI : « Couldn't find any revision » | Credential `github` absent ou token expiré | Refaire l'étape A5 |
| CD : pod `Pending` | Plus assez de place sur le nœud | Normal pendant 1-2 min : le cluster ajoute un 2ᵉ nœud. `kubectl describe pod` pour voir |
| CD : pod `CrashLoopBackOff` | L'application plante au démarrage | `kubectl logs -n banque-dev deploy/account-service --previous` |
| Pods redémarrés tous en même temps | Le nœud spot a été repris par Google | Rien à faire, Kubernetes recrée tout |
