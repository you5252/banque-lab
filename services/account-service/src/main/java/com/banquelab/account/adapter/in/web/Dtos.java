package com.banquelab.account.adapter.in.web;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Objets JSON échangés avec l'extérieur (jamais les objets du domaine directement). */
record TransferRequest(
        @NotNull UUID fromAccountId,
        @NotNull UUID toAccountId,
        @NotNull @Positive @Digits(integer = 15, fraction = 2) BigDecimal amount,
        @NotBlank @Size(min = 3, max = 3) String currency) {
}

record TransferResponse(
        UUID transferId,
        UUID fromAccountId,
        UUID toAccountId,
        BigDecimal amount,
        String currency,
        Instant occurredAt) {
}

record AccountResponse(
        UUID id,
        String iban,
        BigDecimal balance,
        String currency,
        String status) {
}
