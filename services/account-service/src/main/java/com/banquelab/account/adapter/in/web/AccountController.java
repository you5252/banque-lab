package com.banquelab.account.adapter.in.web;

import com.banquelab.account.application.port.in.GetAccountQuery;
import com.banquelab.account.application.port.in.TransferMoneyUseCase;
import com.banquelab.account.application.port.in.TransferMoneyUseCase.TransferCommand;
import com.banquelab.account.domain.model.Account;
import com.banquelab.account.domain.model.AccountId;
import com.banquelab.account.domain.model.Money;
import com.banquelab.account.domain.model.TransferCompleted;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/** Adaptateur ENTRANT REST : traduit HTTP/JSON <-> objets du domaine. */
@RestController
@RequestMapping("/api")
class AccountController {

    private final GetAccountQuery getAccountQuery;
    private final TransferMoneyUseCase transferMoneyUseCase;

    AccountController(GetAccountQuery getAccountQuery, TransferMoneyUseCase transferMoneyUseCase) {
        this.getAccountQuery = getAccountQuery;
        this.transferMoneyUseCase = transferMoneyUseCase;
    }

    @GetMapping("/accounts/{id}")
    AccountResponse getAccount(@PathVariable UUID id) {
        Account account = getAccountQuery.getAccount(AccountId.of(id));
        return new AccountResponse(
                account.getId().value(),
                account.getIban(),
                account.getBalance().amount(),
                account.getBalance().currency(),
                account.getStatus().name());
    }

    @PostMapping("/transfers")
    @ResponseStatus(HttpStatus.CREATED)
    TransferResponse transfer(@Valid @RequestBody TransferRequest request) {
        var command = new TransferCommand(
                AccountId.of(request.fromAccountId()),
                AccountId.of(request.toAccountId()),
                new Money(request.amount(), request.currency()));
        TransferCompleted done = transferMoneyUseCase.transfer(command);
        return new TransferResponse(
                done.transferId(),
                done.from().value(),
                done.to().value(),
                done.amount().amount(),
                done.amount().currency(),
                done.occurredAt());
    }
}
