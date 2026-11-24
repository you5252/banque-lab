# 🏦 BanqueLab

Projet d'apprentissage dans le domaine bancaire, construit à deux (une dev + une testeuse), pour monter en compétence sur la stack la plus demandée en ESN et en banque.

| Domaine | Technologies |
|---|---|
| Back | Java 21, Spring Boot 3.5, Spring Data JPA, Flyway, PostgreSQL 16 |
| Traitements de masse | Spring Batch 5 |
| Messagerie | Apache Kafka (producer / consumer), Kafka UI |
| Architecture | Microservices, chacun en **architecture hexagonale** (vérifiée par ArchUnit) |
| Front | Angular 22 (à créer au Sprint 6) |
| Tests | JUnit 5, AssertJ, Cucumber/Gherkin (FR), Playwright, Selenium, k6 (charge) |
| DevOps | Docker, Jenkins (pipeline CI + pipeline CD), Kubernetes (Kustomize) |

👉 **Commencez par [`docs/ROADMAP.md`](docs/ROADMAP.md)** : toutes les tâches, avec une explication adaptée à chaque profil.

## 📂 Organisation

```
banque-lab/
├── services/
│   ├── account-service/   comptes + virements (REST, JPA, producer Kafka)   ✅ fourni
│   ├── batch-service/     import massif d'opérations (Spring Batch)         ✅ fourni
│   └── fraud-service/     détection de fraude (consumer Kafka)              🛠️ Sprint 4
├── frontend/              application Angular 22                            🛠️ Sprint 6
├── tests/
│   ├── playwright/        tests API (fournis) puis UI
│   ├── perf/              tests de charge k6
│   └── selenium/                                                            🛠️ Sprint 6
├── ci/                    Jenkinsfile.ci et Jenkinsfile.cd
├── k8s/base/              manifestes Kubernetes
├── db/init/               création des bases au 1er démarrage de PostgreSQL
├── scripts/               génération de la grosse base et du gros CSV, création des issues
└── docs/                  roadmap, architecture, guides
```

## 🚀 Démarrage rapide

**Pré-requis** : JDK 21, Maven 3.9+, Docker Desktop, Node 22, Python 3.

```bash
# 1. Infrastructure locale (PostgreSQL, Kafka, Kafka UI)
docker compose up -d

# 2. API des comptes : crée les tables au démarrage (Flyway)
cd services/account-service
mvn spring-boot:run
#   → Swagger : http://localhost:8081/swagger-ui.html

# 3. Dans un autre terminal : la grosse base (200 000 clients, 500 000 comptes, 5 M d'opérations)
docker exec -it banque-postgres psql -U banque -d banque_account -f /scripts/generate-data.sql

# 4. Tests d'API Playwright
cd tests/playwright && npm install && npx playwright install && npm run test:api
```

**Comptes de démo** (identifiants fixes, créés par le script SQL) :

| Compte | UUID | Particularité |
|---|---|---|
| n°1 | `8a8bb7cd-343a-a2ad-99b7-d762030857a2` | actif, 1 000 000 € |
| n°2 | `693a9fdd-4c2f-d070-0968-fba0d07ff3c0` | actif, 1 000 000 € |
| n°50 | `e0af5865-757b-3f2a-8296-7f6177aae43d` | **bloqué** |

Interfaces : Kafka UI → http://localhost:8090 · PostgreSQL → `localhost:5432` (banque / banque)

## 🔐 Mettre le projet sur GitHub (dépôt **privé**)

```bash
cd banque-lab
git init -b main
git add .
git commit -m "Initialisation du projet BanqueLab"
```

**Option A, avec la CLI GitHub (`gh`)** :
```bash
gh auth login
gh repo create banque-lab --private --source=. --push
gh api -X PUT repos/{owner}/banque-lab/collaborators/PSEUDO_DE_TA_SOEUR -f permission=push
```

**Option B, avec le site** : github.com → **New repository** → nom `banque-lab` → cocher **Private** → *Create* (sans README). Ensuite :
```bash
git remote add origin https://github.com/TON_PSEUDO/banque-lab.git
git push -u origin main
```
Puis **Settings → Collaborators → Add people** → pseudo de ta sœur. Elle reçoit une invitation par e-mail et doit l'accepter. Un dépôt privé reste invisible pour tout le monde sauf vous deux, et un compte GitHub gratuit suffit.

**Ensuite** : protéger `main` et créer les issues (tâche S0-1 de la roadmap) :
```bash
./scripts/create-github-issues.sh
```

## 🤝 Règles de travail à deux
1. On ne pousse jamais directement sur `main` : branche → Pull Request → relecture par l'autre → merge.
2. Nommage des branches : `feature/S1-2-historique`, `test/S1-4-scenarios`, `fix/...`.
3. Messages de commit : `S1-2: ajoute la pagination de l'historique`.
4. Une PR = une tâche. Elle doit être petite et relisible en 15 min.
5. Les mesures et les difficultés vont dans `docs/journal.md`.

## 📚 Documentation
- [`docs/ROADMAP.md`](docs/ROADMAP.md) : les tâches, sprint par sprint
- [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) : microservices + hexagonal, expliqué simplement
- [`docs/GUIDE-JAVA8-A-21.md`](docs/GUIDE-JAVA8-A-21.md) : ce qui a changé depuis Java 8 / Spring Boot 1-2
- [`docs/GUIDE-ANGULAR-6-A-22.md`](docs/GUIDE-ANGULAR-6-A-22.md) : ce qui a changé depuis Angular 6
- [`docs/GUIDE-TESTS.md`](docs/GUIDE-TESTS.md) : le guide de la testeuse (bases, Gherkin, Playwright, Selenium, glossaire)
