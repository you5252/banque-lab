Feature: Liste des commandes
  En tant qu'utilisateur
  Je veux consulter et rechercher les commandes
  Afin de retrouver rapidement une commande

  Background:
    Given je suis sur la page des commandes

  Scenario: Affichage de la liste
    Then le tableau des commandes contient au moins 1 ligne

  Scenario Outline: Recherche par client
    When je recherche le client "<client>"
    Then toutes les lignes affichées concernent le client "<client>"

    Examples:
      | client |
      | Dupont |
      | Martin |

  Scenario: Recherche sans résultat
    When je recherche le client "ClientInconnu"
    Then le message "Aucun résultat" est affiché

  Scenario: Ouverture du détail
    When je clique sur la première commande
    Then la page de détail de la commande est affichée