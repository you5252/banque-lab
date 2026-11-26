package com.amaniaya.e2e.tests;

import com.amaniaya.e2e.pages.OrdersListPage;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OrdersTest extends BaseTest {

    @Test
    void recherche_client_inconnu_affiche_aucun_resultat() {
        OrdersListPage page = new OrdersListPage(driver).open();
        page.searchClient("ClientInconnu");
        assertEquals("Aucun résultat", page.getEmptyMessage());
    }

    @Test
    void recherche_client_existant() {
        // TODO : rechercher "Dupont" puis vérifier que chaque ligne contient "Dupont"
    }

    @Test
    void clic_sur_une_ligne_ouvre_le_detail() {
        // TODO : clickFirstRow() puis vérifier l'id et le montant avec OrderDetailPage
    }
}