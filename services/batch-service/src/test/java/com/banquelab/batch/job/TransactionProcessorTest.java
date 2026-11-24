package com.banquelab.batch.job;

import com.banquelab.batch.model.ImportedTransaction;
import com.banquelab.batch.model.TransactionCsvLine;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionProcessorTest {

    private final TransactionProcessor processor = new TransactionProcessor();

    private TransactionCsvLine line(String amount, String type) {
        return new TransactionCsvLine(UUID.randomUUID().toString(), "FR7600000000000000000000001",
                new BigDecimal(amount), "EUR", type, LocalDate.of(2026, 1, 15), "Test");
    }

    @Test
    void ligne_valide_est_conservee() {
        ImportedTransaction result = processor.process(line("-42.10", "CARTE"));
        assertThat(result).isNotNull();
        assertThat(result.suspicious()).isFalse();
    }

    @Test
    void montant_de_10000_ou_plus_est_suspect() {
        assertThat(processor.process(line("10000.00", "VIREMENT")).suspicious()).isTrue();
        assertThat(processor.process(line("9999.99", "VIREMENT")).suspicious()).isFalse();
    }

    @Test
    void montant_nul_ou_type_inconnu_est_filtre() {
        assertThat(processor.process(line("0.00", "CARTE"))).isNull();
        assertThat(processor.process(line("10.00", "CRYPTO"))).isNull();
    }
}
