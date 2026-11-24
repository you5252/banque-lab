#!/usr/bin/env bash
# Crée une issue GitHub par tâche de docs/ROADMAP.md (titre, labels, description complète).
# Pré-requis : CLI GitHub installée et connectée (gh auth login), lancé à la racine du dépôt.
# Essai sans rien créer :  DRY_RUN=1 ./scripts/create-github-issues.sh
set -euo pipefail

ROADMAP="docs/ROADMAP.md"
DRY_RUN="${DRY_RUN:-0}"

if [[ "$DRY_RUN" != "1" ]]; then
  echo "Création des labels…"
  gh label create dev    --color 1D76DB --description "Développeuse"           --force >/dev/null
  gh label create test   --color 0E8A16 --description "Testeuse"               --force >/dev/null
  gh label create binome --color D93F0B --description "À faire en binôme"      --force >/dev/null
  for s in 0 1 2 3 4 5 6 7 8 9; do
    gh label create "sprint-$s" --color C5DEF5 --force >/dev/null
  done
fi

# Découpe la roadmap : chaque bloc commence par "### Sx-y" et finit au titre suivant.
awk '
  /^### S[0-9]+-[0-9]+/ { if (title) print title "\x1f" body "\x1e"; title=$0; body=""; next }
  /^## / || /^---$/     { if (title) { print title "\x1f" body "\x1e"; title="" } next }
  title                 { body = body $0 "\n" }
  END                   { if (title) print title "\x1f" body "\x1e" }
' "$ROADMAP" | while IFS=$'\x1f' read -r -d $'\x1e' heading body; do
  heading="${heading#$'\n'}"
  id=$(sed -E 's/^### (S[0-9]+-[0-9]+).*/\1/' <<<"$heading")
  sprint=$(sed -E 's/^S([0-9]+)-.*/\1/' <<<"$id")
  title=$(sed -E 's/^### //' <<<"$heading")

  labels="sprint-$sprint"
  [[ "$heading" == *DEV* ]]    && labels="$labels,dev"
  [[ "$heading" == *TEST* ]]   && labels="$labels,test"
  [[ "$heading" == *BINÔME* ]] && labels="$labels,binome"

  if [[ "$DRY_RUN" == "1" ]]; then
    echo "[$labels] $title"
  else
    gh issue create --title "$title" --label "$labels" \
      --body "$body"$'\n---\n'"_Tâche issue de \`docs/ROADMAP.md\`._" >/dev/null
    echo "✔ $title"
    sleep 1   # évite la limite de débit de l'API GitHub
  fi
done

echo "Terminé."
