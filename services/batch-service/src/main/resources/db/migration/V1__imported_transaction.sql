CREATE TABLE imported_transaction (
    transaction_id  UUID PRIMARY KEY,
    account_iban    VARCHAR(34)   NOT NULL,
    amount          NUMERIC(19,2) NOT NULL,
    currency        VARCHAR(3)    NOT NULL,
    type            VARCHAR(20)   NOT NULL,
    operation_date  DATE          NOT NULL,
    label           VARCHAR(255),
    suspicious      BOOLEAN       NOT NULL,
    imported_at     TIMESTAMP     NOT NULL
);
