package com.banquelab.account.domain.exception;

import com.banquelab.account.domain.model.AccountId;

public class AccountNotFoundException extends DomainException {
    public AccountNotFoundException(AccountId id) {
        super("Compte introuvable : " + id.value());
    }
}
