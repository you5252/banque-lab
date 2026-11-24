package com.banquelab.account.application.port.in;

import com.banquelab.account.domain.model.AccountId;
import com.banquelab.account.domain.model.Money;
import com.banquelab.account.domain.model.TransferCompleted;

import java.util.Objects;

/** Port ENTRANT : "ce que l'application sait faire" vu de l'extérieur (REST, Kafka, batch...). */
public interface TransferMoneyUseCase {

    TransferCompleted transfer(TransferCommand command);

    record TransferCommand(AccountId from, AccountId to, Money amount) {
        public TransferCommand {
            Objects.requireNonNull(from);
            Objects.requireNonNull(to);
            Objects.requireNonNull(amount);
            if (from.equals(to)) {
                throw new IllegalArgumentException("Impossible de virer vers le même compte");
            }
        }
    }
}
