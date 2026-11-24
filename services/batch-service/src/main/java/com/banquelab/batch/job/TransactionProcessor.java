package com.banquelab.batch.job;

import com.banquelab.batch.model.ImportedTransaction;
import com.banquelab.batch.model.TransactionCsvLine;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

/**
 * Étape "process" : valide et enrichit chaque ligne.
 * Retourner null = la ligne est FILTRÉE (comptée dans filterCount, pas écrite).
 */
@Component
public class TransactionProcessor implements ItemProcessor<TransactionCsvLine, ImportedTransaction> {

    static final BigDecimal SUSPICIOUS_THRESHOLD = new BigDecimal("10000.00");
    private static final Set<String> KNOWN_TYPES = Set.of("CARTE", "VIREMENT", "PRELEVEMENT", "RETRAIT");

    @Override
    public ImportedTransaction process(TransactionCsvLine line) {
        if (line.amount() == null || line.amount().signum() == 0) {
            return null;
        }
        if (!KNOWN_TYPES.contains(line.type())) {
            return null;
        }
        if (line.accountIban() == null || !line.accountIban().startsWith("FR76")) {
            return null;
        }
        boolean suspicious = line.amount().abs().compareTo(SUSPICIOUS_THRESHOLD) >= 0;
        return new ImportedTransaction(
                UUID.fromString(line.transactionId()),
                line.accountIban(),
                line.amount(),
                line.currency(),
                line.type(),
                line.operationDate(),
                line.label(),
                suspicious);
    }
}
