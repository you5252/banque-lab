package com.banquelab.account.domain;

import com.banquelab.account.domain.exception.AccountNotActiveException;
import com.banquelab.account.domain.exception.InsufficientFundsException;
import com.banquelab.account.domain.model.Account;
import com.banquelab.account.domain.model.AccountId;
import com.banquelab.account.domain.model.AccountStatus;
import com.banquelab.account.domain.model.Money;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Test unitaire pur : aucun Spring, aucune base. Ultra rapide (quelques ms). */
class AccountTest {

    private Account account(String balance, AccountStatus status) {
        return new Account(AccountId.of(UUID.randomUUID()), "FR7600000000000000000000001",
                Money.of(balance, "EUR"), status);
    }

    @Test
    void retrait_diminue_le_solde() {
        Account account = account("100.00", AccountStatus.ACTIVE);

        account.withdraw(Money.of("30.50", "EUR"));

        assertThat(account.getBalance()).isEqualTo(Money.of("69.50", "EUR"));
    }

    @Test
    void retrait_superieur_au_solde_est_refuse() {
        Account account = account("100.00", AccountStatus.ACTIVE);

        assertThatThrownBy(() -> account.withdraw(Money.of("100.01", "EUR")))
                .isInstanceOf(InsufficientFundsException.class);
        assertThat(account.getBalance()).isEqualTo(Money.of("100.00", "EUR"));
    }

    @Test
    void compte_bloque_refuse_les_depots() {
        Account account = account("100.00", AccountStatus.BLOCKED);

        assertThatThrownBy(() -> account.deposit(Money.of("10.00", "EUR")))
                .isInstanceOf(AccountNotActiveException.class);
    }

    @Test
    void montant_negatif_refuse() {
        Account account = account("100.00", AccountStatus.ACTIVE);

        assertThatThrownBy(() -> account.deposit(Money.of("-5.00", "EUR")))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
