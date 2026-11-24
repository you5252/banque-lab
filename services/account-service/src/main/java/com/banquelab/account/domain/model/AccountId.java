package com.banquelab.account.domain.model;

import java.util.Objects;
import java.util.UUID;

/** Identifiant typé : évite de confondre un id de compte avec un id de client. */
public record AccountId(UUID value) {

    public AccountId {
        Objects.requireNonNull(value, "value");
    }

    public static AccountId of(UUID value) {
        return new AccountId(value);
    }
}
