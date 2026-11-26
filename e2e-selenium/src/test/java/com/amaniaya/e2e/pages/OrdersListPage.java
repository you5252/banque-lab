package com.amaniaya.e2e.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

public class OrdersListPage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By searchInput  = By.cssSelector("[data-testid='orders-search']");
    private final By rows         = By.cssSelector("[data-testid='order-row']");
    private final By emptyMessage = By.cssSelector("[data-testid='orders-empty']");

    public OrdersListPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public OrdersListPage open() {
        // Par défaut : la maquette HTML locale (pas besoin d'Angular).
        // Pour tester la vraie appli : mvn test -DordersUrl=http://localhost:4200/orders
        String url = System.getProperty("ordersUrl");
        if (url == null || url.isBlank()) {
            url = getClass().getResource("/orders.html").toString();
        }
        driver.get(url);
        wait.until(ExpectedConditions.visibilityOfElementLocated(searchInput));
        return this;
    }

    public void searchClient(String client) {
        WebElement input = driver.findElement(searchInput);
        input.clear();
        input.sendKeys(client);
    }

    public List<WebElement> getRows() {
        return driver.findElements(rows);
    }

    public String getEmptyMessage() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(emptyMessage)).getText();
    }

    public void clickFirstRow() {
        wait.until(ExpectedConditions.elementToBeClickable(rows)).click();
    }
}
