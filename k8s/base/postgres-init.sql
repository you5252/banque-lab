-- Exécuté automatiquement au PREMIER démarrage du conteneur PostgreSQL.
-- Une base par microservice : chaque service est propriétaire de ses données.
CREATE DATABASE banque_account;
CREATE DATABASE banque_batch;
CREATE DATABASE banque_fraud;
