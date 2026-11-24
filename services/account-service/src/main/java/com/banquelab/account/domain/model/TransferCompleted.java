package com.banquelab.account.domain.model;

import java.time.Instant;
import java.util.UUID;

/** Événement métier : "un virement a eu lieu". Il sera publié dans Kafka. */
public record TransferCompleted(
        UUID transferId,
        AccountId from,
        AccountId to,
        Money amount,
        Instant occurredAt) {
}
