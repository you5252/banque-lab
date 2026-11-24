-- Schéma initial du service "comptes".
-- Flyway exécute ce fichier une seule fois puis le note dans la table flyway_schema_history.
-- Règle d'or : on ne modifie JAMAIS un fichier déjà exécuté, on crée V2__..., V3__...

CREATE TABLE customer (
    id          UUID PRIMARY KEY,
    first_name  VARCHAR(100) NOT NULL,
    last_name   VARCHAR(100) NOT NULL,
    email       VARCHAR(255) NOT NULL UNIQUE,
    segment     VARCHAR(20)  NOT NULL,
    created_at  TIMESTAMP    NOT NULL
);

CREATE TABLE account (
    id          UUID PRIMARY KEY,
    customer_id UUID          NOT NULL REFERENCES customer (id),
    iban        VARCHAR(34)   NOT NULL UNIQUE,
    balance     NUMERIC(19,2) NOT NULL,
    currency    VARCHAR(3)    NOT NULL,
    status      VARCHAR(20)   NOT NULL,
    version     BIGINT        NOT NULL DEFAULT 0,
    created_at  TIMESTAMP     NOT NULL
);

CREATE TABLE account_transaction (
    id          UUID PRIMARY KEY,
    account_id  UUID          NOT NULL REFERENCES account (id),
    amount      NUMERIC(19,2) NOT NULL,
    type        VARCHAR(20)   NOT NULL,
    label       VARCHAR(255),
    created_at  TIMESTAMP     NOT NULL
);

-- ⚠️ Volontairement : AUCUN index sur account.customer_id ni sur account_transaction.account_id.
-- Avec des millions de lignes, certaines requêtes seront très lentes → tâche S2-1.
