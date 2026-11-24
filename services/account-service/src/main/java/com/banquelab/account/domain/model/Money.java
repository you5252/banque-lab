package com.banquelab.account.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Montant + devise. "record" = classe immuable (Java 16+) : constructeur, equals,
 * hashCode, toString et accesseurs amount()/currency() sont générés automatiquement.
 * En Java 8 il aurait fallu ~50 lignes pour la même chose.
 */
public record Money(BigDecimal amount, String currency) {

    // "Constructeur compact" : validation + normalisation avant l'affectation des champs
    public Money {
        Objects.requireNonNull(amount, "amount");
        Objects.requireNonNull(currency, "currency");
        amount = amount.setScale(2, RoundingMode.HALF_EVEN);
    }

    public static Money of(String amount, String currency) {
        return new Money(new BigDecimal(amount), currency);
    }

    public Money plus(Money other) {
        checkSameCurrency(other);
        return new Money(amount.add(other.amount), currency);
    }

    public Money minus(Money other) {
        checkSameCurrency(other);
        return new Money(amount.subtract(other.amount), currency);
    }

    public boolean isPositive() {
        return amount.signum() > 0;
    }

    public boolean isLessThan(Money other) {
        checkSameCurrency(other);
        return amount.compareTo(other.amount) < 0;
    }

    private void checkSameCurrency(Money other) {
        if (!currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                    "Devises différentes : %s / %s".formatted(currency, other.currency));
        }
    }
}
