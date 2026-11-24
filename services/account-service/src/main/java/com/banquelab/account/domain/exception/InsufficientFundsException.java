package com.banquelab.account.domain.exception;

import com.banquelab.account.domain.model.AccountId;
import com.banquelab.account.domain.model.Money;

public class InsufficientFundsException extends DomainException {
    public InsufficientFundsException(AccountId id, Money balance, Money requested) {
        super("Solde insuffisant sur le compte %s : solde=%s, demandé=%s"
                .formatted(id.value(), balance.amount(), requested.amount()));
    }
}
