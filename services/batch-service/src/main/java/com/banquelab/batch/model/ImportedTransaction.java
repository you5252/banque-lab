package com.banquelab.batch.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** Une opération validée, prête à être écrite en base. */
public record ImportedTransaction(
        UUID transactionId,
        String accountIban,
        BigDecimal amount,
        String currency,
        String type,
        LocalDate operationDate,
        String label,
        boolean suspicious) {
}
