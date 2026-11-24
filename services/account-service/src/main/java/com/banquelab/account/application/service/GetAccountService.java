package com.banquelab.account.application.service;

import com.banquelab.account.application.port.in.GetAccountQuery;
import com.banquelab.account.application.port.out.LoadAccountPort;
import com.banquelab.account.domain.exception.AccountNotFoundException;
import com.banquelab.account.domain.model.Account;
import com.banquelab.account.domain.model.AccountId;
import org.springframework.transaction.annotation.Transactional;

public class GetAccountService implements GetAccountQuery {

    private final LoadAccountPort loadAccountPort;

    public GetAccountService(LoadAccountPort loadAccountPort) {
        this.loadAccountPort = loadAccountPort;
    }

    @Override
    @Transactional(readOnly = true)
    public Account getAccount(AccountId id) {
        return loadAccountPort.load(id).orElseThrow(() -> new AccountNotFoundException(id));
    }
}
