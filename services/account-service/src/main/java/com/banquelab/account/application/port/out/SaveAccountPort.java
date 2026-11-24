package com.banquelab.account.application.port.out;

import com.banquelab.account.domain.model.Account;

public interface SaveAccountPort {
    void save(Account account);
}
