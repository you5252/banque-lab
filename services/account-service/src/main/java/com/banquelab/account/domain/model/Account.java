package com.banquelab.account.domain.model;

import com.banquelab.account.domain.exception.AccountNotActiveException;
import com.banquelab.account.domain.exception.InsufficientFundsException;

import java.util.Objects;

/**
 * Cœur du métier : un compte bancaire.
 * Aucune annotation Spring/JPA ici : le domaine ne dépend d'AUCUN framework
 * (règle vérifiée automatiquement par HexagonalArchitectureTest).
 */
public class Account {

    private final AccountId id;
    private final String iban;
    private Money balance;
    private AccountStatus status;

    public Account(AccountId id, String iban, Money balance, AccountStatus status) {
        this.id = Objects.requireNonNull(id);
        this.iban = Objects.requireNonNull(iban);
        this.balance = Objects.requireNonNull(balance);
        this.status = Objects.requireNonNull(status);
    }

    public void withdraw(Money amount) {
        ensureActive();
        requirePositive(amount);
        if (balance.isLessThan(amount)) {
            throw new InsufficientFundsException(id, balance, amount);
        }
        balance = balance.minus(amount);
    }

    public void deposit(Money amount) {
        ensureActive();
        requirePositive(amount);
        balance = balance.plus(amount);
    }

    private void ensureActive() {
        if (status != AccountStatus.ACTIVE) {
            throw new AccountNotActiveException(id, status);
        }
    }

    private static void requirePositive(Money amount) {
        if (!amount.isPositive()) {
            throw new IllegalArgumentException("Le montant doit être strictement positif");
        }
    }

    public AccountId getId() { return id; }
    public String getIban() { return iban; }
    public Money getBalance() { return balance; }
    public AccountStatus getStatus() { return status; }
}
