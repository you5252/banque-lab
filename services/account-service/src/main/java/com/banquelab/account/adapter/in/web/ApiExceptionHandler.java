package com.banquelab.account.adapter.in.web;

import com.banquelab.account.domain.exception.AccountNotActiveException;
import com.banquelab.account.domain.exception.AccountNotFoundException;
import com.banquelab.account.domain.exception.InsufficientFundsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Transforme les erreurs en réponses HTTP au format standard "Problem Details" (RFC 9457),
 * supporté nativement depuis Spring Boot 3.
 * L'héritage de ResponseEntityExceptionHandler gère aussi les erreurs de validation (400).
 *
 * Tâche S1-1 : remplacer les 4 méthodes par une seule avec un "switch" sur le type (Java 21).
 */
@RestControllerAdvice
class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(AccountNotFoundException.class)
    ProblemDetail notFound(AccountNotFoundException e) {
        return problem(HttpStatus.NOT_FOUND, "Compte introuvable", e.getMessage());
    }

    @ExceptionHandler(InsufficientFundsException.class)
    ProblemDetail insufficientFunds(InsufficientFundsException e) {
        return problem(HttpStatus.UNPROCESSABLE_ENTITY, "Solde insuffisant", e.getMessage());
    }

    @ExceptionHandler(AccountNotActiveException.class)
    ProblemDetail notActive(AccountNotActiveException e) {
        return problem(HttpStatus.CONFLICT, "Compte non actif", e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail badRequest(IllegalArgumentException e) {
        return problem(HttpStatus.BAD_REQUEST, "Requête invalide", e.getMessage());
    }

    private static ProblemDetail problem(HttpStatus status, String title, String detail) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setTitle(title);
        return pd;
    }
}
