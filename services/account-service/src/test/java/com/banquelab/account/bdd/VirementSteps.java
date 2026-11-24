package com.banquelab.account.bdd;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * "Glue" entre les phrases Gherkin (fichier .feature) et le code.
 * Cucumber associe une phrase à une méthode grâce au TEXTE de l'annotation
 * (le mot-clé Étant donné / Quand / Alors n'a pas d'importance pour la correspondance).
 * {string}, {bigdecimal}, {word}, {int} sont des paramètres capturés dans la phrase.
 */
public class VirementSteps {

    @Autowired private TestRestTemplate rest;
    @Autowired private JdbcTemplate jdbc;

    private final Map<String, UUID> comptes = new HashMap<>();
    private int dernierStatutHttp;

    @Before
    public void nettoyerLaBase() {
        jdbc.update("DELETE FROM account_transaction");
        jdbc.update("DELETE FROM account");
        jdbc.update("DELETE FROM customer");
    }

    @Given("le compte {string} avec un solde de {bigdecimal} {word}")
    public void creerCompte(String nom, BigDecimal solde, String devise) {
        UUID clientId = UUID.randomUUID();
        UUID compteId = UUID.randomUUID();
        Timestamp maintenant = Timestamp.from(Instant.now());
        jdbc.update("INSERT INTO customer (id, first_name, last_name, email, segment, created_at) VALUES (?,?,?,?,?,?)",
                clientId, "Test", nom, nom.toLowerCase() + "-" + clientId + "@test.fr", "PARTICULIER", maintenant);
        jdbc.update("INSERT INTO account (id, customer_id, iban, balance, currency, status, version, created_at) VALUES (?,?,?,?,?,?,?,?)",
                compteId, clientId, "FR76" + compteId.toString().replace("-", "").substring(0, 23),
                solde, devise, "ACTIVE", 0, maintenant);
        comptes.put(nom, compteId);
    }

    @When("je vire {bigdecimal} {word} du compte {string} vers le compte {string}")
    public void virer(BigDecimal montant, String devise, String source, String destination) {
        Map<String, Object> body = Map.of(
                "fromAccountId", comptes.get(source),
                "toAccountId", comptes.get(destination),
                "amount", montant,
                "currency", devise);
        dernierStatutHttp = rest.postForEntity("/api/transfers", body, String.class)
                .getStatusCode().value();
    }

    @Then("le virement est accepté")
    public void virementAccepte() {
        assertThat(dernierStatutHttp).isEqualTo(201);
    }

    @Then("le virement est refusé avec le code {int}")
    public void virementRefuse(int codeAttendu) {
        assertThat(dernierStatutHttp).isEqualTo(codeAttendu);
    }

    @Then("le solde du compte {string} est de {bigdecimal} {word}")
    public void verifierSolde(String nom, BigDecimal soldeAttendu, String devise) {
        BigDecimal solde = jdbc.queryForObject(
                "SELECT balance FROM account WHERE id = ?", BigDecimal.class, comptes.get(nom));
        assertThat(solde).isEqualByComparingTo(soldeAttendu);
    }
}
