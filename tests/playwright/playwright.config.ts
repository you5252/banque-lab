import { defineConfig } from '@playwright/test';

/**
 * Configuration Playwright.
 * - projet "api" : tests d'API (fonctionne dès maintenant, sans front)
 * - projet "ui"  : à activer au Sprint 6 quand le front Angular existe (tâche S6-4)
 */
export default defineConfig({
  testDir: './tests',
  timeout: 30_000,
  retries: process.env.CI ? 1 : 0,
  reporter: [['list'], ['html', { open: 'never' }], ['junit', { outputFile: 'results/junit.xml' }]],
  use: {
    trace: 'retain-on-failure',
  },
  projects: [
    {
      name: 'api',
      testMatch: /api\/.*\.spec\.ts/,
      use: { baseURL: process.env.API_URL ?? 'http://localhost:8081' },
    },
    // {
    //   name: 'ui',
    //   testMatch: /ui\/.*\.spec\.ts/,
    //   use: { baseURL: process.env.FRONT_URL ?? 'http://localhost:4200', browserName: 'chromium' },
    // },
  ],
});
