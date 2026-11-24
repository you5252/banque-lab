package com.banquelab.account.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Représentation "base de données" d'un compte. Elle reste dans l'adaptateur :
 * le domaine ne connaît pas JPA.
 * Remarque : la colonne "version" existe en base mais n'est pas encore mappée (tâche S3-2).
 */
@Entity
@Table(name = "account")
public class AccountJpaEntity {

    @Id
    private UUID id;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(nullable = false, length = 34)
    private String iban;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false, length = 20)
    private String status;

    protected AccountJpaEntity() {
        // requis par JPA
    }

    public UUID getId() { return id; }
    public UUID getCustomerId() { return customerId; }
    public String getIban() { return iban; }
    public BigDecimal getBalance() { return balance; }
    public String getCurrency() { return currency; }
    public String getStatus() { return status; }

    public void setBalance(BigDecimal balance) { this.balance = balance; }
    public void setStatus(String status) { this.status = status; }
}
