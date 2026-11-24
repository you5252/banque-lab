package com.banquelab.account.application.port.in;

import com.banquelab.account.domain.model.Account;
import com.banquelab.account.domain.model.AccountId;

public interface GetAccountQuery {
    Account getAccount(AccountId id);
}
