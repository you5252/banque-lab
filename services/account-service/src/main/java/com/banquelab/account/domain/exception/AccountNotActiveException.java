package com.banquelab.account.domain.exception;

import com.banquelab.account.domain.model.AccountId;
import com.banquelab.account.domain.model.AccountStatus;

public class AccountNotActiveException extends DomainException {
    public AccountNotActiveException(AccountId id, AccountStatus status) {
        super("Le compte %s n'est pas actif (statut=%s)".formatted(id.value(), status));
    }
}
