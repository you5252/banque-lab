import { Page, Locator } from '@playwright/test';
export class OrdersListPage {
    readonly searchInput: Locator;
    readonly rows: Locator;
    readonly emptyMessage: Locator;
    constructor(private page: Page) {
        this.searchInput = page.getByTestId('orders-search');
        this.rows = page.getByTestId('order-row');
        this.emptyMessage = page.getByTestId('orders-empty');
    }
    async goto() {
        await this.page.goto('/orders');
    }
    async searchClient(client: string) {
        await this.searchInput.fill(client);
    }
}