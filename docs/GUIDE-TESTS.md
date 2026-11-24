# 🧪 Le guide de la testeuse

Ce guide part de zéro. Garde-le ouvert pendant les premiers sprints.

## 1. Pourquoi on teste, et à quel niveau

```
            /\        Tests E2E / UI (Playwright, Selenium)
           /  \       → peu nombreux, lents, mais proches de l'utilisateur
          /----\      Tests d'API / intégration (Cucumber, Playwright API)
         /      \     → le meilleur rapport effort / confiance
        /--------\    Tests unitaires (JUnit, écrits surtout par la dev)
       /__________\   → très nombreux, très rapides
```
C'est la **pyramide des tests** : beaucoup de petits tests rapides en bas, peu de gros tests lents en haut. Un test UI qui casse dès qu'un bouton change de couleur coûte cher à maintenir.

## 2. Les techniques à connaître (et à citer en entretien)

| Technique | En une phrase | Exemple BanqueLab |
|---|---|---|
| **Classes d'équivalence** | On regroupe les valeurs qui se comportent pareil et on en teste une par groupe | Montant : négatif / 0 / normal / supérieur au solde |
| **Valeurs limites** | Les bugs se cachent aux frontières | Seuil de fraude 10 000 € → tester 9 999.99 / 10 000.00 / 10 000.01 |
| **Table de décision** | On croise les conditions | Compte actif ou bloqué × solde suffisant ou non |
| **Tests exploratoires** | On explore librement mais avec un objectif et une durée (*charter*) | « 30 min pour casser le virement avec des données bizarres » |
| **Test de non-régression** | On rejoue les anciens tests pour vérifier que rien n'est cassé | La CI le fait à chaque Pull Request |
| **Invariant** | Une vérité qui doit toujours tenir | La somme des soldes ne change jamais avec des virements |

## 3. Écrire du Gherkin (BDD)

```gherkin
# language: fr
Fonctionnalité: Virement entre deux comptes

  Contexte:
    Étant donné le compte "COMPTE-A" avec un solde de 1000.00 EUR

  Scénario: Virement refusé pour solde insuffisant
    Quand je vire 5000.00 EUR du compte "COMPTE-A" vers le compte "COMPTE-B"
    Alors le virement est refusé avec le code 422

  Plan du scénario: Détection de fraude selon le montant
    Quand je vire <montant> EUR du compte "COMPTE-A" vers le compte "COMPTE-B"
    Alors une alerte de fraude <resultat>

    Exemples:
      | montant  | resultat         |
      | 9999.99  | n'est pas levée  |
      | 10000.00 | est levée        |
```

**Les bonnes pratiques :**
- ✅ **Déclaratif** (ce qu'on veut) : « Quand je vire 200 € vers COMPTE-B »
- ❌ **Impératif** (comment on clique) : « Quand je clique sur le champ montant, je tape 200, je clique sur Valider »
- Un scénario = **un** comportement. Si tu as 3 « Quand », découpe.
- Le scénario doit être compréhensible par quelqu'un du métier (un conseiller bancaire).
- `Contexte` = les étapes communes à tous les scénarios du fichier.

**Comment ça s'exécute :** chaque phrase est reliée à une méthode Java dans `VirementSteps.java` grâce à son texte. `{string}` capture un texte entre guillemets, `{bigdecimal}` un nombre décimal, `{int}` un entier et `{word}` un mot.

## 4. Tester une API

Les éléments d'un appel :
- **Méthode** : `GET` (lire), `POST` (créer), `PUT`/`PATCH` (modifier), `DELETE` (supprimer)
- **URL** : `http://localhost:8081/api/accounts/{id}`
- **Corps (body)** en JSON : `{"fromAccountId": "...", "amount": 10.00, "currency": "EUR"}`
- **Code retour** : 2xx = succès · 4xx = erreur du client (ta requête) · 5xx = erreur du serveur (**toujours un bug**)

Ce qu'on vérifie : le code, le contenu de la réponse, **l'effet réel** (le solde a-t-il bougé en base ?) et l'absence d'effet en cas d'erreur (un virement refusé ne doit rien débiter).

## 5. Playwright ou Selenium ?

| | Playwright | Selenium |
|---|---|---|
| Langages | TypeScript/JS (le plus utilisé), Java, Python, .NET | Java (le plus utilisé en banque), Python, C#… |
| Attente des éléments | **automatique** | à gérer soi-même (`WebDriverWait`) |
| Tests d'API | oui, intégrés | non (on utilise RestAssured) |
| Outils | enregistreur `codegen`, *trace viewer*, captures et vidéos | Selenium IDE, Grid |
| En entreprise | en forte croissance sur les nouveaux projets | encore très présent sur l'existant |

👉 Apprends Playwright en premier (plus simple, plus moderne), puis Selenium (Sprint 6) pour être employable partout.

**Sélecteurs, du plus robuste au plus fragile :**
1. `getByRole('button', { name: 'Valider' })` : ce que voit l'utilisateur
2. `getByTestId('virement-montant')` : un attribut ajouté exprès pour les tests
3. `getByText('Solde')`
4. ❌ CSS ou XPath du type `div > div:nth-child(3) > span` : casse au moindre changement

**Page Object Model :** une classe par page, qui regroupe ses sélecteurs et ses actions (`pageVirement.saisirMontant(200)`). Si la page change, on ne modifie qu'un seul fichier.

## 6. Tests de performance
- **Charge** : le nombre normal d'utilisateurs, est-ce que ça tient ?
- **Stress** : on augmente jusqu'à ce que ça casse, où est la limite ?
- **Endurance** : la charge normale pendant des heures, y a-t-il une fuite mémoire ?
- Les indicateurs : **p95 / p99** (temps de réponse), **débit** (requêtes/s), **taux d'erreur**.

## 7. Écrire un bon rapport d'anomalie (issue GitHub, label `bug`)
```
Titre : [Virement] Un virement de 0.001 € est accepté et arrondi à 0.00

Environnement : local, commit a1b2c3d
Pré-requis : compte de démo n°1 actif
Étapes :
  1. POST /api/transfers avec amount = 0.001
Résultat attendu : 400, montant invalide
Résultat obtenu : 201, virement de 0.00 € créé
Gravité : majeure (crée des opérations vides)
Pièces jointes : requête et réponse complètes
```

## 8. Glossaire du monde dev

| Mot | Explication simple |
|---|---|
| **Back-end / API** | La partie invisible qui contient les règles et parle à la base de données |
| **Front-end** | Ce que l'utilisateur voit dans son navigateur (ici Angular) |
| **Base de données / SQL** | Des tableaux Excel géants et le langage pour les interroger |
| **Microservice** | Une petite application qui fait un seul métier (comptes, fraude…) |
| **Kafka / topic** | Un journal de messages partagé, avec des rubriques |
| **Batch** | Un traitement de masse lancé à heure fixe, sans interface |
| **Thread** | Un « ouvrier » qui exécute du code. Plusieurs ouvriers en même temps = concurrence |
| **Race condition** | Un bug qui n'arrive que quand deux ouvriers touchent la même chose en même temps |
| **Docker / image / conteneur** | Une boîte qui contient l'appli et tout ce qu'il lui faut pour tourner partout pareil |
| **Kubernetes / pod** | Le chef d'orchestre des boîtes. Un pod = une boîte qui tourne |
| **CI / CD** | Vérifier automatiquement chaque modification / la déployer automatiquement |
| **Jenkins** | Le robot qui exécute la CI/CD |
| **Pull Request (PR)** | « Voici ma modification, relis-la avant qu'on l'intègre » |
| **Mock** | Une fausse version d'un composant, pour tester sans lui |
| **Flaky test** | Un test qui passe ou échoue au hasard : à corriger en priorité |
