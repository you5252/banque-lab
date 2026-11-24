package com.banquelab.account.application.port.out;

import com.banquelab.account.domain.model.Account;
import com.banquelab.account.domain.model.AccountId;

import java.util.Optional;

/** Port SORTANT : "ce dont l'application a besoin" (ici lire un compte), sans dire comment. */
public interface LoadAccountPort {
    Optional<Account> load(AccountId id);
}
