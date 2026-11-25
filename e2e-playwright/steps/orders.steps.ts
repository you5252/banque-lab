import { expect } from '@playwright/test';
import { createBdd } from 'playwright-bdd';
import { OrdersListPage } from '../pages/OrdersListPage';

const { Given, When, Then } = createBdd();

// Lignes du tableau des commandes (sélecteur à adapter)
const ROWS = 'table tbody tr';

Given('je suis sur la page des commandes', async ({ page }) => {
    await new OrdersListPage(page).goto();
});

Then('le tableau des commandes contient au moins {int} ligne', async ({ page }, min: number) => {
    const rows = page.locator(ROWS);
    await expect(rows.first()).toBeVisible();
    expect(await rows.count()).toBeGreaterThanOrEqual(min);
});

When('je recherche le client {string}', async ({ page }, client: string) => {
    await new OrdersListPage(page).searchClient(client);
});

Then('toutes les lignes affichées concernent le client {string}', async ({ page }, client: string) => {
    const rows = page.locator(ROWS);
    await expect(rows.first()).toContainText(client);
    for (const row of await rows.all()) {
        await expect(row).toContainText(client);
    }
});

Then('le message {string} est affiché', async ({ page }, msg: string) => {
    await expect(new OrdersListPage(page).emptyMessage).toHaveText(msg);
});

When('je clique sur la première commande', async ({ page }) => {
    await page.locator(ROWS).first().click();
});

Then('la page de détail de la commande est affichée', async ({ page }) => {
    await expect(page).toHaveURL(/\/orders\/.+/); // à adapter à l'URL réelle
});