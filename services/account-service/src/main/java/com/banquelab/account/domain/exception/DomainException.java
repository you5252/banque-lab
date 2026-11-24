package com.banquelab.account.domain.exception;

/** Erreur métier. (Tâche S1-1 : la transformer en "sealed class".) */
public abstract class DomainException extends RuntimeException {
    protected DomainException(String message) {
        super(message);
    }
}
