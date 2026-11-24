# 🗺️ Feuille de route : monter en compétence en construisant BanqueLab

**Qui fait quoi :**

| Badge | Pour qui | Idée |
|---|---|---|
| 👩‍💻 **DEV** | Développeuse (Java 8 / Angular 6, 11 ans d'expérience) | Tu connais déjà le métier de dev : on se concentre sur **ce qui a changé** et ce qu'on attend d'une senior aujourd'hui. |
| 🧪 **TEST** | Testeuse junior | On part de zéro, avec des explications imagées. Tu apprends à **casser** l'appli de façon méthodique. |
| 👯 **BINÔME** | Vous deux ensemble | En pair programming : l'une tape au clavier, l'autre guide, et vous changez toutes les 25 min. |

**Chaque tâche contient :** 🎯 l'objectif · 💡 une explication simple · 🛠️ quoi faire · ✅ « terminé quand… »

**Rythme conseillé :** 1 sprint = 1 à 2 semaines, avec 5 à 8 h par personne et par semaine. Tout le parcours dure environ 4 à 5 mois.

**Méthode de travail (comme en entreprise) :**
1. Une tâche = une *issue* GitHub (le script `scripts/create-github-issues.sh` les crée toutes).
2. Une branche par tâche : `feature/S1-2-historique` ou `test/S1-4-scenarios-virement`.
3. Une Pull Request (PR) relue par l'autre avant d'être fusionnée sur `main`.
   - La testeuse relit les PR de la dev en se demandant « comment je pourrais casser ça ? ».
   - La dev relit les PR de la testeuse : lisibilité, sélecteurs robustes, assertions.

---

## Sprint 0 · Mise en place (≈ 3 jours)

### S0-1 · Créer le dépôt GitHub privé et inviter ta sœur 👯 BINÔME
🎯 Avoir un espace de travail partagé et protégé.
💡 *La branche `main`, c'est la version officielle. On la protège pour que personne (même pas toi !) ne puisse y pousser du code sans relecture. C'est la règle n°1 en entreprise.*
🛠️
- Suivre la section « Mettre le projet sur GitHub » du `README.md` (dépôt en **privé**).
- Settings → Collaborators → *Add people* → le pseudo GitHub de ta sœur (rôle *Write*).
- Settings → Branches → *Add rule* sur `main` : « Require a pull request before merging » + « Require approvals : 1 ».
- Créer un *Project* GitHub (vue Board) avec les colonnes À faire / En cours / En revue / Terminé.
- Lancer `scripts/create-github-issues.sh` pour créer toutes les tâches.

✅ Ta sœur a accepté l'invitation, voit les issues et ne peut pas pousser directement sur `main`.

### S0-2 · Installer l'environnement de dev 👩‍💻 DEV
🎯 Faire tourner `account-service` en local.
💡 Java 21 est une version LTS (support long). Le passage de `javax.*` à `jakarta.*` est la première différence que tu verras dans Spring Boot 3.
🛠️ Installer JDK 21 (Temurin), Maven 3.9, Docker Desktop et IntelliJ IDEA. Ensuite :
`docker compose up -d` → `cd services/account-service && mvn spring-boot:run` → ouvrir http://localhost:8081/swagger-ui.html

✅ La page Swagger s'affiche et `mvn verify` est vert (tests unitaires + ArchUnit + Cucumber).

### S0-3 · Installer l'environnement de test 🧪 TEST
🎯 Savoir lancer l'application et l'appeler « à la main ».
💡 *Une **API**, c'est un guichet : on envoie une demande (une **requête** HTTP) et on reçoit une réponse avec un **code** : 200 = OK, 201 = créé, 400 = ta demande est mal remplie, 404 = introuvable, 422 = demande comprise mais refusée par une règle métier, 500 = l'appli a planté (c'est un bug !).*
🛠️
- Installer Docker Desktop, Node.js 22, VS Code, Git et [Bruno](https://www.usebruno.com/) (ou Postman).
- Lancer `docker compose up -d`, puis demander à ta sœur de démarrer l'API (ou utiliser le Dockerfile).
- Dans Swagger, essayer `GET /api/accounts/8a8bb7cd-343a-a2ad-99b7-d762030857a2`.
- Créer une collection Bruno « BanqueLab » avec 3 requêtes et la versionner dans `tests/bruno/`.

✅ Tu sais obtenir un 200, un 400, un 404 et un 422, et tu sais expliquer chacun.

### S0-4 · Générer la grosse base de données 👯 BINÔME
🎯 Se mettre dans les conditions d'une vraie banque : des millions de lignes.
💡 *Avec 10 lignes, tout est rapide. Avec 5 millions, les erreurs de conception sautent aux yeux. C'est là qu'on apprend la performance.*
🛠️ `docker exec -it banque-postgres psql -U banque -d banque_account -f /scripts/generate-data.sql`
Installer DBeaver, se connecter (localhost:5432, banque/banque), explorer les tables.

✅ Vous avez noté dans `docs/journal.md` le temps de génération et la taille de la base (`SELECT pg_size_pretty(pg_database_size('banque_account'));`).

---

## Sprint 1 · Java 21, Spring Boot 3 et architecture hexagonale

### S1-1 · Moderniser le code avec Java 21 👩‍💻 DEV
🎯 Utiliser `sealed`, le *pattern matching* et les *switch expressions*.
💡 Lis d'abord `docs/GUIDE-JAVA8-A-21.md`. Une classe `sealed` déclare la **liste fermée** de ses sous-classes. Le compilateur peut alors vérifier qu'un `switch` traite tous les cas, sans `default`.
🛠️
- Transformer `DomainException` en `sealed ... permits InsufficientFundsException, AccountNotFoundException, AccountNotActiveException` (les sous-classes deviennent `final`).
- Dans `ApiExceptionHandler`, remplacer les 3 handlers métier par une seule méthode :
  `return switch (e) { case AccountNotFoundException ex -> ...; case InsufficientFundsException ex -> ...; ... };`
- Ajouter une 4ᵉ exception et constater que le compilateur t'oblige à la traiter.

✅ Le code compile, les tests passent et tu peux expliquer à ta sœur pourquoi c'est plus sûr qu'avant.

### S1-2 · Endpoint « historique des opérations » (paginé) 👩‍💻 DEV
🎯 Créer une fonctionnalité complète en respectant l'hexagone.
💡 Lis `docs/ARCHITECTURE.md`. Tu vas créer **un port entrant** (le cas d'usage), **un port sortant** (le besoin de lecture), **un adaptateur JPA** et **un adaptateur REST**. Le test ArchUnit échouera si tu prends un raccourci.
🛠️ `GET /api/accounts/{id}/transactions?page=0&size=20` trié par date décroissante, avec `Page`/`Slice` côté adaptateur et un objet de pagination maison côté domaine.
✅ L'endpoint répond, `HexagonalArchitectureTest` est vert et un test `@WebMvcTest` couvre le contrôleur.

### S1-3 · Enregistrer l'historique lors d'un virement 👩‍💻 DEV
🎯 Un virement doit créer 2 lignes dans `account_transaction` (un débit et un crédit).
🛠️ Nouveau port sortant `RecordTransactionPort`, avec son adaptateur. Le tout dans la **même transaction** que la mise à jour des soldes.
✅ Le scénario Cucumber de ta sœur (S1-4) vérifie que les 2 lignes existent.

### S1-4 · Écrire des scénarios Gherkin 🧪 TEST
🎯 Décrire le comportement attendu en français, sous une forme exécutable.
💡 *Gherkin, c'est une recette en 3 temps : **Étant donné** (la situation de départ), **Quand** (l'action), **Alors** (le résultat attendu). Chaque phrase est reliée à du code Java (le fichier `VirementSteps.java`) qui l'exécute vraiment. Un scénario = un seul comportement.*
🛠️ Dans `services/account-service/src/test/resources/features/virement.feature` :
- Ajouter les scénarios « compte bloqué » (code 409), « montant négatif » (400) et « virement vers le même compte » (400).
- Écrire un **Plan du scénario** avec un tableau d'**Exemples** de montants : 0.01 / 999.99 / 1000.00 / 1000.01.
- Lancer `mvn test` et ouvrir `target/cucumber-report.html`.
- S'il manque une phrase côté Java, ta sœur t'aide à écrire la méthode (c'est un très bon premier pas en Java !).

✅ Au moins 6 scénarios verts, et un rapport HTML montré en démo.

### S1-5 · Cahier de recette et tests exploratoires 🧪 TEST
🎯 Savoir tester méthodiquement sans automatiser.
💡 *Deux techniques de base :*
- *Les **classes d'équivalence** : on regroupe les valeurs qui se comportent pareil (montant négatif, nul, normal, trop grand) et on en teste une par groupe.*
- *Les **valeurs limites** : les bugs se cachent aux frontières. Si la limite est 1000, on teste 999.99, 1000 et 1000.01.*

🛠️ Créer `docs/tests/cahier-recette.md` avec un tableau ID | Pré-requis | Étapes | Résultat attendu | Résultat obtenu | Statut. Couvrir au moins 15 cas sur l'API virement. Chaque anomalie trouvée devient une issue GitHub avec le label `bug`.
✅ 15 cas joués et au moins une anomalie (ou une amélioration) remontée proprement.

### S1-6 · Première revue de code croisée 👯 BINÔME
🎯 Apprendre à relire et à être relue.
🛠️ La testeuse relit la PR de S1-2 avec cette checklist : les noms sont-ils clairs ? Y a-t-il des tests ? Que se passe-t-il avec une page négative ou une taille de 10 000 ? La dev relit la PR de S1-4.
✅ Chaque PR a reçu au moins 2 commentaires constructifs, qui ont été traités.

---

## Sprint 2 · Performance base de données (gros volume)

### S2-1 · Diagnostiquer et corriger une requête lente 👩‍💻 DEV
🎯 Lire un plan d'exécution et poser le bon index.
💡 Sans index sur `account_transaction.account_id`, PostgreSQL lit les 5 millions de lignes (*Seq Scan*) pour trouver les opérations d'un seul compte. Un index, c'est comme l'index d'un livre.
🛠️
- Appeler ton endpoint S1-2 et mesurer le temps de réponse.
- Dans DBeaver : `EXPLAIN (ANALYZE, BUFFERS) SELECT ... WHERE account_id = ... ORDER BY created_at DESC LIMIT 20;`
- Créer `V2__index_transactions.sql` avec un index composite `(account_id, created_at DESC)`, puis comparer.
- Bonus : comparer la pagination par OFFSET (page 10 000 = lent) et la pagination par curseur (*keyset*).

✅ Le temps avant/après est noté dans `docs/journal.md`, avec les deux plans d'exécution.

### S2-2 · Traquer le problème N+1 👩‍💻 DEV
🎯 Le piège n°1 de JPA en entreprise.
💡 Charger 50 comptes, puis leur client un par un = 1 + 50 requêtes. Active `spring.jpa.show-sql` ou, mieux, un compteur de requêtes Hibernate.
🛠️ Endpoint `GET /api/customers/{id}/accounts` (avec le dernier solde et les 3 dernières opérations). L'écrire naïvement, compter les requêtes, puis corriger avec `JOIN FETCH`, `@EntityGraph` ou une projection DTO.
✅ Un test vérifie le nombre de requêtes exécutées.

### S2-3 · Premier test de charge 🧪 TEST
🎯 Mesurer la performance comme un pro.
💡 *Un test de charge simule beaucoup d'utilisateurs en même temps. On regarde surtout le **p95** : le temps sous lequel passent 95 % des requêtes. La moyenne cache les clients mécontents !*
🛠️ Installer k6 (ou passer par l'image Docker, voir l'en-tête du script). Adapter `tests/perf/historique-et-virements.js` pour appeler l'endpoint historique, puis le lancer **avant** et **après** l'index de S2-1.
✅ Un tableau comparatif (p95, requêtes/s, % d'erreurs) dans `docs/tests/rapport-perf.md`.

### S2-4 · Pool de connexions sous charge 👯 BINÔME
🎯 Comprendre HikariCP.
💡 Le pool, ce sont 10 « lignes téléphoniques » vers la base. S'il y a 50 appels simultanés, 40 attendent.
🛠️ Pendant le test k6 de ta sœur, observer `/actuator/metrics/hikaricp.connections.pending`. Essayer `maximum-pool-size` à 2, 10 puis 50 : plus grand n'est pas toujours mieux !
✅ Vous savez expliquer pourquoi la valeur retenue est la bonne.

---

## Sprint 3 · Concurrence et threads 🔥 (le sujet qui fait la différence en entretien)

### S3-1 · Révéler la race condition dans les virements 👯 BINÔME
🎯 Prouver par un test qu'il y a un bug de concurrence.
💡 *Pour la testeuse : imagine deux caissiers qui lisent en même temps le même solde de 100 €, retirent chacun 10 € et écrivent 90 €. Au lieu de 80 €, il reste 90 € : la banque a « créé » 10 €. C'est ce qu'on appelle une **mise à jour perdue** (lost update).*
🛠️ Écrire un test d'intégration, de préférence avec **Testcontainers** (un vrai PostgreSQL dans Docker) :
```java
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    for (int i = 0; i < 100; i++) {
        executor.submit(() -> transferMoneyUseCase.transfer(new TransferCommand(a, b, Money.of("1.00", "EUR"))));
    }
}
// Invariant : soldeA + soldeB doit être identique avant et après
```
✅ Le test **échoue** de manière reproductible. Bravo, vous avez trouvé un vrai bug !

### S3-2 · Corriger avec un verrou optimiste 👩‍💻 DEV
🎯 La correction la plus courante en entreprise.
💡 On mappe la colonne `version` avec `@Version`. Hibernate fait alors `UPDATE ... WHERE id=? AND version=?`. Si quelqu'un est passé avant, 0 ligne est modifiée et une `OptimisticLockException` est levée. On **réessaie** quelques fois.
🛠️ `@Version` dans `AccountJpaEntity`, puis un mécanisme de retry (Spring Retry `@Retryable`, ou une boucle maison avec backoff). Attention : le retry doit envelopper **toute** la transaction.
✅ Le test S3-1 passe. Tu sais expliquer quand l'optimiste est le bon choix (peu de conflits).

### S3-3 · Verrou pessimiste et deadlock 👩‍💻 DEV
🎯 Comprendre l'alternative et son piège.
💡 `SELECT ... FOR UPDATE` bloque la ligne. Mais si le virement 1 verrouille A puis B pendant que le virement 2 verrouille B puis A, chacun attend l'autre : c'est un **deadlock**.
🛠️ Créer une branche d'expérimentation avec `@Lock(PESSIMISTIC_WRITE)`, provoquer le deadlock avec des virements A→B et B→A en parallèle, puis le corriger en verrouillant **toujours dans le même ordre** (le plus petit UUID d'abord).
✅ Un court ADR (Architecture Decision Record) `docs/adr/001-verrouillage.md` explique le choix entre optimiste et pessimiste.

### S3-4 · Threads virtuels (Java 21) 👩‍💻 DEV + 🧪 TEST
🎯 Mesurer l'apport des virtual threads.
💡 Un thread classique coûte environ 1 Mo et Tomcat en a 200 par défaut. Un thread virtuel est très léger : on peut en avoir des millions. C'est utile quand le code **attend** beaucoup (base, appels HTTP), pas quand il calcule.
🛠️ La dev ajoute un endpoint qui simule un appel externe lent (`Thread.sleep(200)`). La testeuse lance k6 avec 500 utilisateurs virtuels, d'abord avec `spring.threads.virtual.enabled=false` puis `true`. Bonus dev : le *pinning* avec un bloc `synchronized` en Java 21 (et `ReentrantLock` comme correction).
✅ Les graphiques avant/après figurent dans `rapport-perf.md`.

### S3-5 · Appels parallèles avec CompletableFuture 👩‍💻 DEV
🎯 Endpoint « synthèse client » : comptes + total des opérations du mois + nombre d'alertes, en parallèle.
🛠️ `CompletableFuture.supplyAsync(..., executor)`, puis `allOf`, `orTimeout` et `exceptionally`. Ensuite, comparer avec la *Structured Concurrency* (preview en Java 21, à expérimenter).
✅ Le temps total est proche de l'appel le plus lent, pas de la somme des trois.

### S3-6 · Tester un invariant métier sous charge 🧪 TEST
🎯 Vérifier qu'une règle n'est **jamais** violée, même sous stress.
💡 *Un **invariant**, c'est une vérité qui doit toujours tenir. Ici : « l'argent ne se crée ni ne disparaît ». La somme de tous les soldes doit être identique avant et après n'importe quel nombre de virements.*
🛠️ Requête `SELECT sum(balance) FROM account;` avant le test, puis décommenter la partie virements du script k6, lancer 50 utilisateurs pendant 1 min, et refaire la somme. À faire **avant** et **après** la correction S3-2.
✅ Un rapport montre l'écart avant la correction et 0 € d'écart après.

---

## Sprint 4 · Kafka (producer et consumer)

### S4-1 · Comprendre le producer existant 👯 BINÔME
💡 *Pour la testeuse : Kafka, c'est un **journal de bord** partagé. Les applications y écrivent des messages (les **producers**) dans des rubriques (les **topics**), et d'autres applications les lisent à leur rythme (les **consumers**). Personne n'attend personne.*
🛠️ Faire des virements, puis ouvrir Kafka UI (http://localhost:8090) → topic `bank.transfers.v1`. Observer la clé, la partition et l'offset. Faire 10 virements depuis le même compte et vérifier qu'ils sont tous dans la même partition (et comprendre pourquoi).
✅ Vous savez expliquer partition, offset, clé et ordre des messages.

### S4-2 · Créer `fraud-service` (consumer) 👩‍💻 DEV
🎯 Un 2ᵉ microservice hexagonal qui écoute les virements.
🛠️ Nouveau module `services/fraud-service`, avec sa base `banque_fraud`. L'adaptateur entrant est un `@KafkaListener`. Règles : montant ≥ 10 000 € **ou** plus de 5 virements en 1 minute depuis le même compte. Les alertes sont enregistrées en base et publiées sur `bank.fraud-alerts.v1`.
✅ Une alerte apparaît dans Kafka UI après un gros virement.

### S4-3 · Gestion des erreurs : retry et Dead Letter Topic 👩‍💻 DEV
💡 Un message illisible (*poison pill*) ne doit pas bloquer toute la file. Après N essais, on le range dans `bank.transfers.v1.DLT`.
🛠️ `DefaultErrorHandler` + `DeadLetterPublishingRecoverer` + `FixedBackOff`. Ajouter l'**idempotence** côté consumer (une table `processed_event` pour ignorer un message déjà traité).
✅ Un message JSON invalide se retrouve dans le DLT et le consumer continue de tourner.

### S4-4 · Le problème base de données ↔ Kafka : pattern Outbox 👩‍💻 DEV
💡 Aujourd'hui, `TransferMoneyService` envoie le message **pendant** la transaction. Si Kafka accepte mais que la base fait ensuite un rollback, on a annoncé un virement qui n'a jamais eu lieu. Dans l'autre sens, on peut perdre l'événement.
🛠️ Écrire l'événement dans une table `outbox` **dans la même transaction**, puis un *poller* `@Scheduled` (ou Debezium, en bonus) le publie et le marque comme envoyé.
✅ Un test montre qu'un rollback ne publie rien.

### S4-5 · Concurrence des consumers 👩‍💻 DEV
🛠️ `concurrency = 3` sur le listener avec 3 partitions, puis 6. Observer dans les logs quel thread lit quelle partition, et lancer 2 instances de `fraud-service` pour voir le *rebalancing*.
✅ Tu sais expliquer pourquoi 6 consumers pour 3 partitions = 3 consumers au chômage.

### S4-6 · Tester les règles de fraude avec les valeurs limites 🧪 TEST
🛠️ Fichier `fraude.feature` avec un **Plan du scénario** : 9 999.99 € (pas d'alerte), 10 000.00 € (alerte), 10 000.01 € (alerte). Même chose pour 5 virements (pas d'alerte) contre 6 (alerte) en 1 minute. Ta sœur t'aide pour les *steps* (attendre un message Kafka avec **Awaitility**).
✅ Les scénarios sont verts dans le rapport Cucumber.

### S4-7 · Test d'intégration avec Testcontainers Kafka 👯 BINÔME
🛠️ Un vrai Kafka dans Docker le temps du test (`@Testcontainers` + `@ServiceConnection`).
✅ `mvn verify` lance le test sans `docker compose` préalable.

---

## Sprint 5 · Spring Batch

### S5-1 · Premier import massif 👯 BINÔME
💡 *Pour la testeuse : un **batch**, c'est un traitement de nuit qui avale un énorme fichier (ici 1 million d'opérations) par **paquets** de 1 000 lignes. Si ça plante au milieu, on peut reprendre là où on s'était arrêté.*
🛠️
```bash
python3 scripts/generate_transactions_csv.py --lines 1000000 --out data/transactions.csv
cd services/batch-service && mvn spring-boot:run -Dspring-boot.run.arguments="inputFile=../../data/transactions.csv"
```
Explorer les tables `batch_job_execution` et `batch_step_execution` (read_count, write_count, filter_count, skip_count…).
✅ Durée et compteurs notés. Vous savez expliquer la différence entre *filtrée* et *rejetée*.

### S5-2 · Lignes rejetées dans un fichier d'erreurs 👩‍💻 DEV
🛠️ `SkipListener` qui écrit les lignes rejetées (avec la raison) dans `rejets-<date>.csv`. Ajouter une politique de *retry* sur les erreurs SQL transitoires.
✅ Le fichier de rejets contient exactement les lignes invalides générées.

### S5-3 · Accélérer le batch 👩‍💻 DEV
💡 `FlatFileItemReader` **n'est pas thread-safe** : à plusieurs threads, il faut l'envelopper (`SynchronizedItemStreamReader`) ou partitionner.
🛠️ Version A : *multi-threaded step* (`taskExecutor`). Version B : *partitioning* (découper le fichier en 4 et lancer 4 workers). Mesurer avec 1, 2, 4 et 8 threads, et tester aussi la taille de chunk (100 / 1 000 / 5 000).
✅ Un tableau des temps est publié. Tu sais expliquer pourquoi le gain plafonne.

### S5-4 · Job « relevés mensuels » 👩‍💻 DEV
🛠️ Lire `account_transaction` du mois (base `banque_account` : une **2ᵉ DataSource**) avec un `JdbcPagingItemReader`, agréger par compte et écrire un CSV par compte (ou un PDF en bonus).
✅ Un relevé correct est produit pour le compte de démo n°1.

### S5-5 · Redémarrage après crash 👯 BINÔME
🛠️ Lancer l'import et le tuer (Ctrl+C) au milieu. Le relancer avec les **mêmes paramètres** et vérifier qu'il reprend au dernier chunk. Comprendre aussi pourquoi `RunIdIncrementer` crée une nouvelle instance.
✅ Pas de doublon en base après reprise. Sinon, c'est un bug à corriger !

### S5-6 · Stratégie de test d'un batch 🧪 TEST
🎯 Concevoir des **jeux de données** pertinents.
🛠️ Créer dans `tests/batch-datasets/` : un fichier vide, un fichier avec seulement l'en-tête, 10 lignes toutes invalides, des doublons d'identifiant, une ligne avec des caractères spéciaux (« Café – Noël ») et 1 million de lignes. Pour chacun : résultat attendu (compteurs) et résultat obtenu.
✅ Une matrice de tests complète et au moins un comportement inattendu remonté.

---

## Sprint 6 · Front Angular 22 + tests UI

### S6-1 · Créer le front 👩‍💻 DEV
💡 Lis `docs/GUIDE-ANGULAR-6-A-22.md`. Depuis Angular 6, tout a changé : plus de `NgModule` obligatoire, des *signals*, le nouveau *control flow* `@if`/`@for`, le mode *zoneless* par défaut et Vitest pour les tests unitaires.
🛠️ `npx @angular/cli@22 new frontend --style=scss --routing` à la racine du dépôt. Ajouter un `proxy.conf.json` vers `localhost:8081`.
✅ `ng serve` affiche la page d'accueil et appelle l'API via le proxy.

### S6-2 · Pages « Mes comptes » et « Historique » 👩‍💻 DEV
🛠️ Composants *standalone*, `signal()`, `computed()`, `inject(HttpClient)` (ou `httpResource`), `@for` avec `track`, pagination. **Ajouter des attributs `data-testid`** sur les éléments importants, pour ta sœur.
✅ La navigation fonctionne et les tests unitaires Vitest passent.

### S6-3 · Formulaire de virement 👩‍💻 DEV
🛠️ Formulaire avec validation (Signal Forms, nouveauté v22, ou Reactive Forms). Afficher les erreurs de l'API (le `title` du ProblemDetail) et un écran de confirmation.
✅ Un virement complet est possible depuis le navigateur.

### S6-4 · Tests UI avec Playwright 🧪 TEST
💡 *Playwright pilote un vrai navigateur comme un robot : il clique, tape et vérifie ce qui s'affiche. Il **attend tout seul** que les éléments apparaissent, ce qui évite les tests « qui ratent une fois sur deux ».*
🛠️ Décommenter le projet `ui` dans `playwright.config.ts`. Écrire le parcours « j'ouvre la liste des comptes → je consulte un compte → je fais un virement → je vois mon nouveau solde ». Utiliser le **Page Object Model** (une classe par page) et des sélecteurs `getByRole` / `getByTestId`. Apprendre `npx playwright codegen` et le *trace viewer*.
✅ 5 tests UI stables (lancés 10 fois de suite sans échec).

### S6-5 · Gherkin + Playwright 🧪 TEST
🛠️ Ajouter `playwright-bdd` et réécrire le parcours virement en `.feature`, en réutilisant les Page Objects.
✅ Le même scénario se lit en français et s'exécute dans le navigateur.

### S6-6 · Le même test en Selenium 🧪 TEST + 👩‍💻 DEV
💡 *Beaucoup de banques utilisent encore Selenium : il faut connaître les deux.*
🛠️ Module `tests/selenium` en Java (Selenium 4 + Cucumber + JUnit 5) avec le même scénario. Comparer le temps d'écriture, la vitesse, la gestion des attentes (`WebDriverWait`) et la stabilité.
✅ Un comparatif Playwright / Selenium d'une page dans `docs/tests/`.

---

## Sprint 7 · Docker et Jenkins (CI)

### S7-1 · Lancer Jenkins en local 👯 BINÔME
🛠️ `docker run -d -p 8088:8080 -v jenkins_home:/var/jenkins_home -v /var/run/docker.sock:/var/run/docker.sock jenkins/jenkins:lts-jdk21`. Installer les plugins *Pipeline*, *Git*, *Docker Pipeline* et *JUnit*, puis déclarer les outils `jdk21` et `maven3`. Le dépôt étant privé, créer un *Personal Access Token* GitHub (droits *repo* en lecture) et l'enregistrer dans les credentials Jenkins.
✅ Jenkins voit le dépôt privé.

### S7-2 · Pipeline CI 👩‍💻 DEV
💡 Lis `ci/Jenkinsfile.ci`, tout est commenté. **CI** = à chaque push, on vérifie automatiquement que tout compile et que les tests passent.
🛠️ Créer un job *Multibranch Pipeline* sur `ci/Jenkinsfile.ci`. Remplacer `TON_COMPTE_DOCKERHUB` par ton compte et ajouter le credential `dockerhub`.
✅ Une PR affiche ✅ ou ❌ selon le résultat de la CI.

### S7-3 · Tests Playwright dans la CI 🧪 TEST
🛠️ Ajouter un stage qui démarre la stack (`docker compose`), lance `npm ci && npx playwright install --with-deps && npm test`, puis publie `results/junit.xml` et le rapport HTML.
✅ Un test qui échoue fait échouer la CI.

### S7-4 · Optimiser les images Docker 👩‍💻 DEV
🛠️ Mesurer la taille actuelle (`docker images`), utiliser les *layered jars* de Spring Boot, comparer avec une image `distroless`, puis scanner les vulnérabilités avec Trivy.
✅ Image plus petite, aucune vulnérabilité critique.

### S7-5 · Qualité : JaCoCo + SonarQube 👩‍💻 DEV
🛠️ Couverture JaCoCo, SonarQube en Docker, stage *Quality Gate* dans la CI (échec si couverture < 70 % ou bug bloquant).
✅ Le tableau de bord SonarQube est consultable.

---

## Sprint 8 · Kubernetes et pipeline CD

### S8-1 · Installer un cluster local 👯 BINÔME
💡 *Pour la testeuse : Docker met une appli dans une boîte (un **conteneur**). Kubernetes est le **chef d'orchestre** qui gère des dizaines de boîtes : il les démarre, les redémarre si elles plantent, en ajoute quand il y a du monde, et répartit le trafic.*
🛠️ Installer `kind` (ou le Kubernetes intégré à Docker Desktop) et `kubectl`. Apprendre les commandes `get pods`, `describe`, `logs` et `port-forward`.
✅ `kubectl get nodes` affiche un nœud *Ready*.

### S8-2 · Déployer l'application 👩‍💻 DEV
🛠️ `kubectl create namespace banque-dev && kubectl apply -k k8s/base -n banque-dev`. Comprendre Deployment, Service, ConfigMap, Secret et les 3 *probes*.
✅ `kubectl port-forward svc/account-service 8081:8081` puis Swagger fonctionne.

### S8-3 · Kafka sur Kubernetes avec Strimzi 👩‍💻 DEV
🛠️ Installer l'opérateur Strimzi et créer un cluster `banque-kafka` (1 nœud KRaft). Déployer `fraud-service`.
✅ Le flux virement → alerte fraude fonctionne dans le cluster.

### S8-4 · Pipeline CD 👩‍💻 DEV
💡 **CD** = déployer automatiquement la version validée par la CI. Lis `ci/Jenkinsfile.cd` : dev automatique, prod avec validation humaine et rollback si le smoke test échoue.
🛠️ Créer le job `banque-lab-cd`, ajouter le credential `kubeconfig`, puis créer des *overlays* Kustomize `dev`, `recette` et `prod` (nombre de replicas, ressources).
✅ Un merge sur `main` déploie tout seul en dev.

### S8-5 · Tests post-déploiement 🧪 TEST
🛠️ Ajouter au pipeline CD un stage qui lance les tests Playwright API sur l'environnement de recette. Provoquer volontairement une panne (mauvaise image) et vérifier le rollback.
✅ Tu sais expliquer à quoi sert chaque *probe* et ce qu'on voit quand l'une d'elles échoue.

### S8-6 · CronJob et autoscaling 👯 BINÔME
🛠️ La dev déploie `batch-service` en `CronJob` (chaque nuit à 2 h) et configure un `HorizontalPodAutoscaler` sur `account-service`. La testeuse lance une charge k6 et vous regardez ensemble les pods se multiplier.
✅ Une capture du passage de 2 à N pods sous charge.

---

## Sprint 9 · Bonus « profil senior » (au choix)

| Tâche | Pour | Sujet |
|---|---|---|
| S9-1 | 👩‍💻 | **Sécurité** : Spring Security + OAuth2/OIDC avec Keycloak (JWT, rôles CLIENT / CONSEILLER) |
| S9-2 | 🧪 | **Tests de sécurité** : OWASP ZAP sur l'API, tests d'accès (un client ne voit pas les comptes d'un autre) |
| S9-3 | 👩‍💻 | **Observabilité** : Micrometer + Prometheus + Grafana, traces distribuées (OpenTelemetry) entre les services |
| S9-4 | 👩‍💻 | **Résilience** : Resilience4j (circuit breaker, rate limiter) sur les appels entre services |
| S9-5 | 👩‍💻 | **Migration vers Spring Boot 4** : le support open source de Spring Boot 3.5 est terminé, la migration est un vrai sujet d'entreprise |
| S9-6 | 🧪 | **Tests de contrat** : Pact ou Spring Cloud Contract entre `account-service` et `fraud-service` |
| S9-7 | 👯 | **Démo finale** : présenter le projet comme en entretien (architecture, choix, chiffres de perf) |

---

## 📓 Rituels
- **Chaque semaine (30 min)** : chacune présente à l'autre ce qu'elle a appris. Expliquer, c'est le meilleur moyen de retenir.
- **`docs/journal.md`** : notez les mesures, les erreurs rencontrées et leurs solutions. C'est votre futur argumentaire d'entretien.
- **À chaque fin de sprint** : une petite démo + une ligne sur votre CV/LinkedIn (« Mise en place d'un pipeline CI/CD Jenkins → Kubernetes »).
