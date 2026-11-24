package com.banquelab.account.application.service;

import com.banquelab.account.application.port.in.TransferMoneyUseCase;
import com.banquelab.account.application.port.out.LoadAccountPort;
import com.banquelab.account.application.port.out.PublishTransferEventPort;
import com.banquelab.account.application.port.out.SaveAccountPort;
import com.banquelab.account.domain.exception.AccountNotFoundException;
import com.banquelab.account.domain.model.Account;
import com.banquelab.account.domain.model.AccountId;
import com.banquelab.account.domain.model.TransferCompleted;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Cas d'usage "virer de l'argent".
 *
 * ⚠️ Ce code contient VOLONTAIREMENT deux problèmes réalistes à découvrir :
 *   - Sprint 3 : un problème de concurrence quand plusieurs virements touchent le même compte en même temps.
 *   - Sprint 4 : un problème de cohérence entre la base de données et Kafka.
 * Ne les corrige pas avant d'arriver à la tâche correspondante dans docs/ROADMAP.md 😉
 */
public class TransferMoneyService implements TransferMoneyUseCase {

    private final LoadAccountPort loadAccountPort;
    private final SaveAccountPort saveAccountPort;
    private final PublishTransferEventPort publishTransferEventPort;

    public TransferMoneyService(LoadAccountPort loadAccountPort,
                                SaveAccountPort saveAccountPort,
                                PublishTransferEventPort publishTransferEventPort) {
        this.loadAccountPort = loadAccountPort;
        this.saveAccountPort = saveAccountPort;
        this.publishTransferEventPort = publishTransferEventPort;
    }

    @Override
    @Transactional
    public TransferCompleted transfer(TransferCommand command) {
        Account from = load(command.from());
        Account to = load(command.to());

        from.withdraw(command.amount());
        to.deposit(command.amount());

        saveAccountPort.save(from);
        saveAccountPort.save(to);

        var event = new TransferCompleted(
                UUID.randomUUID(), from.getId(), to.getId(), command.amount(), Instant.now());
        publishTransferEventPort.publish(event);
        return event;
    }

    private Account load(AccountId id) {
        return loadAccountPort.load(id).orElseThrow(() -> new AccountNotFoundException(id));
    }
}
