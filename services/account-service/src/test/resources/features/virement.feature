# language: fr
Fonctionnalité: Virement entre deux comptes
  En tant que client de la banque
  Je veux virer de l'argent d'un compte vers un autre
  Afin de gérer mon budget

  Contexte:
    Étant donné le compte "COMPTE-A" avec un solde de 1000.00 EUR
    Et le compte "COMPTE-B" avec un solde de 50.00 EUR

  Scénario: Virement accepté
    Quand je vire 200.00 EUR du compte "COMPTE-A" vers le compte "COMPTE-B"
    Alors le virement est accepté
    Et le solde du compte "COMPTE-A" est de 800.00 EUR
    Et le solde du compte "COMPTE-B" est de 250.00 EUR

  Scénario: Virement refusé pour solde insuffisant
    Quand je vire 5000.00 EUR du compte "COMPTE-A" vers le compte "COMPTE-B"
    Alors le virement est refusé avec le code 422
    Et le solde du compte "COMPTE-A" est de 1000.00 EUR
    Et le solde du compte "COMPTE-B" est de 50.00 EUR

  # 👉 Tâche S1-4 (testeuse) : ajouter ici les scénarios
  #    "compte bloqué", "montant négatif", "même compte", puis un "Plan du scénario".
