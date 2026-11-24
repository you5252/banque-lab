package com.banquelab.batch.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Une ligne brute du fichier CSV, telle que lue. */
public record TransactionCsvLine(
        String transactionId,
        String accountIban,
        BigDecimal amount,
        String currency,
        String type,
        LocalDate operationDate,
        String label) {
}
