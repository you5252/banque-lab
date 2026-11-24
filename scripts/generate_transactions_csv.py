#!/usr/bin/env python3
"""
Génère un gros fichier CSV d'opérations bancaires pour le batch d'import (Sprint 5).

Usage :
    python3 scripts/generate_transactions_csv.py --lines 1000000 --out data/transactions.csv

Environ 0,1 % des lignes sont VOLONTAIREMENT invalides (montant illisible, date fausse,
type inconnu, montant à zéro) pour tester les règles de rejet/filtrage du batch.
"""
import argparse
import csv
import os
import random
import uuid
from datetime import date, timedelta

TYPES = ["CARTE", "VIREMENT", "PRELEVEMENT", "RETRAIT"]
LABELS = ["Supermarché", "Loyer", "Salaire", "Électricité", "Restaurant", "Essence", "Abonnement"]


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--lines", type=int, default=1_000_000)
    parser.add_argument("--accounts", type=int, default=500_000)
    parser.add_argument("--out", default="data/transactions.csv")
    parser.add_argument("--seed", type=int, default=42)
    args = parser.parse_args()

    random.seed(args.seed)
    os.makedirs(os.path.dirname(args.out) or ".", exist_ok=True)
    start = date.today() - timedelta(days=365)
    broken = 0

    with open(args.out, "w", newline="", encoding="utf-8") as f:
        writer = csv.writer(f)
        writer.writerow(["transactionId", "accountIban", "amount", "currency", "type", "operationDate", "label"])
        for _ in range(args.lines):
            iban = "FR76" + str(random.randint(1, args.accounts)).zfill(23)
            amount = f"{random.uniform(-1500, 800):.2f}"
            if random.random() < 0.002:  # quelques gros montants "suspects"
                amount = f"{random.uniform(10000, 50000):.2f}"
            op_date = (start + timedelta(days=random.randint(0, 365))).isoformat()
            row = [str(uuid.uuid4()), iban, amount, "EUR", random.choice(TYPES), op_date, random.choice(LABELS)]

            if random.random() < 0.001:
                broken += 1
                defect = random.choice(["amount", "date", "type", "zero"])
                if defect == "amount":
                    row[2] = "douze euros"
                elif defect == "date":
                    row[5] = "2026-13-45"
                elif defect == "type":
                    row[4] = "CRYPTO"
                else:
                    row[2] = "0.00"
            writer.writerow(row)

    print(f"{args.lines} lignes écrites dans {args.out} (dont {broken} volontairement invalides)")


if __name__ == "__main__":
    main()
