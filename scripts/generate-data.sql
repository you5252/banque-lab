-- =====================================================================
--  Génère une GROSSE base réaliste pour account-service.
--  Pré-requis : account-service a démarré au moins une fois (Flyway a créé les tables).
--
--  Lancer :
--    docker exec -it banque-postgres psql -U banque -d banque_account -f /scripts/generate-data.sql
--
--  Volumes par défaut : 200 000 clients, 500 000 comptes, 5 000 000 d'opérations
--  (≈ 1 à 3 minutes, ≈ 1,5 Go). Machine modeste ? Divise les 3 valeurs par 10.
-- =====================================================================
\set nb_clients 200000
\set nb_comptes 500000
\set nb_operations 5000000
\timing on

-- Les identifiants sont DÉTERMINISTES : md5('a1')::uuid est toujours le compte n°1.
-- Pratique pour les tests (Playwright, k6) qui doivent connaître des comptes existants.

INSERT INTO customer (id, first_name, last_name, email, segment, created_at)
SELECT md5('c' || n)::uuid,
       'Prenom' || n,
       'Nom' || n,
       'client' || n || '@banquelab.test',
       (ARRAY['PARTICULIER', 'PREMIUM', 'PROFESSIONNEL'])[1 + n % 3],
       now() - (n % 3650) * interval '1 day'
FROM generate_series(1, :nb_clients) AS n;

INSERT INTO account (id, customer_id, iban, balance, currency, status, version, created_at)
SELECT md5('a' || n)::uuid,
       md5('c' || (1 + n % :nb_clients))::uuid,
       'FR76' || lpad(n::text, 23, '0'),
       round((random() * 20000)::numeric, 2),
       'EUR',
       CASE WHEN n % 50 = 0 THEN 'BLOCKED' WHEN n % 97 = 0 THEN 'CLOSED' ELSE 'ACTIVE' END,
       0,
       now() - (n % 2000) * interval '1 day'
FROM generate_series(1, :nb_comptes) AS n;

-- Les comptes 1 à 10 sont garantis ACTIFS et bien approvisionnés (comptes de démo/tests).
UPDATE account SET status = 'ACTIVE', balance = 1000000.00
WHERE iban BETWEEN 'FR76' || lpad('1', 23, '0') AND 'FR76' || lpad('10', 23, '0');

INSERT INTO account_transaction (id, account_id, amount, type, label, created_at)
SELECT gen_random_uuid(),
       md5('a' || (1 + floor(random() * :nb_comptes))::int)::uuid,
       round(((random() - 0.6) * 2000)::numeric, 2),
       (ARRAY['CARTE', 'VIREMENT', 'PRELEVEMENT', 'RETRAIT'])[1 + floor(random() * 4)::int],
       'Operation ' || n,
       now() - random() * interval '730 days'
FROM generate_series(1, :nb_operations) AS n;

ANALYZE;

SELECT 'clients' AS table_name, count(*) FROM customer
UNION ALL SELECT 'comptes', count(*) FROM account
UNION ALL SELECT 'operations', count(*) FROM account_transaction;
