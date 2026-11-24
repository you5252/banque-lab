package com.banquelab.account.adapter.out.persistence;

import com.banquelab.account.application.port.out.LoadAccountPort;
import com.banquelab.account.application.port.out.SaveAccountPort;
import com.banquelab.account.domain.exception.AccountNotFoundException;
import com.banquelab.account.domain.model.Account;
import com.banquelab.account.domain.model.AccountId;
import com.banquelab.account.domain.model.AccountStatus;
import com.banquelab.account.domain.model.Money;
import org.springframework.stereotype.Component;

import java.util.Optional;

/** Adaptateur SORTANT : implémente les ports de persistance avec Spring Data JPA. */
@Component
class AccountPersistenceAdapter implements LoadAccountPort, SaveAccountPort {

    private final AccountJpaRepository repository;

    AccountPersistenceAdapter(AccountJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Account> load(AccountId id) {
        return repository.findById(id.value()).map(AccountPersistenceAdapter::toDomain);
    }

    @Override
    public void save(Account account) {
        AccountJpaEntity entity = repository.findById(account.getId().value())
                .orElseThrow(() -> new AccountNotFoundException(account.getId()));
        entity.setBalance(account.getBalance().amount());
        entity.setStatus(account.getStatus().name());
        repository.save(entity);
    }

    private static Account toDomain(AccountJpaEntity e) {
        return new Account(
                AccountId.of(e.getId()),
                e.getIban(),
                new Money(e.getBalance(), e.getCurrency()),
                AccountStatus.valueOf(e.getStatus()));
    }
}
