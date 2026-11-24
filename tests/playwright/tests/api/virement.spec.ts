import { test, expect } from '@playwright/test';
import { compteDeDemo } from './comptes';

/**
 * Tests d'API avec Playwright.
 * Pré-requis : docker compose up -d, account-service démarré, scripts/generate-data.sql exécuté.
 * Lancer : npm run test:api
 */
test.describe('API Virements', () => {
  const compteA = compteDeDemo(1);
  const compteB = compteDeDemo(2);

  test('un virement valide débite la source et crédite la destination', async ({ request }) => {
    const avantA = await (await request.get(`/api/accounts/${compteA}`)).json();
    const avantB = await (await request.get(`/api/accounts/${compteB}`)).json();

    const reponse = await request.post('/api/transfers', {
      data: { fromAccountId: compteA, toAccountId: compteB, amount: 1.0, currency: 'EUR' },
    });

    expect(reponse.status()).toBe(201);
    const apresA = await (await request.get(`/api/accounts/${compteA}`)).json();
    const apresB = await (await request.get(`/api/accounts/${compteB}`)).json();
    expect(Number(apresA.balance)).toBeCloseTo(Number(avantA.balance) - 1, 2);
    expect(Number(apresB.balance)).toBeCloseTo(Number(avantB.balance) + 1, 2);
  });

  test('un compte inexistant renvoie 404 au format Problem Details', async ({ request }) => {
    const reponse = await request.get('/api/accounts/00000000-0000-0000-0000-000000000000');

    expect(reponse.status()).toBe(404);
    const corps = await reponse.json();
    expect(corps.title).toBe('Compte introuvable');
  });

  test('un montant négatif est refusé (400)', async ({ request }) => {
    const reponse = await request.post('/api/transfers', {
      data: { fromAccountId: compteA, toAccountId: compteB, amount: -10, currency: 'EUR' },
    });

    expect(reponse.status()).toBe(400);
  });

  // 👉 Tâche S1-5 / S2-3 (testeuse) : ajouter les cas
  //    - devise sur 2 lettres, champ manquant, UUID mal formé
  //    - virement vers le même compte
  //    - virement depuis un compte BLOQUÉ (le compte n°50 est bloqué dans les données générées)
});
