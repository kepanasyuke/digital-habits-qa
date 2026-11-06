package ru.digitalhabits.support;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.qameta.allure.Allure;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Hooks {

    private static final Logger log = LoggerFactory.getLogger(Hooks.class);

    private final DbClient dbClient;
    private final ApiClient apiClient;

    public Hooks(DbClient dbClient, ApiClient apiClient) {
        this.dbClient = dbClient;
        this.apiClient = apiClient;
    }

    @Before(order = 0)
    public void prepareDatabase(Scenario scenario) {
        log.info("Сценарий: {} — чищу таблицы", scenario.getName());
        dbClient.truncateAll();
    }

    @After
    public void attachLastResponse(Scenario scenario) {
        Response response = apiClient.lastResponse();
        if (response != null) {
            String details = response.getStatusLine() + System.lineSeparator() + response.getBody().asString();
            Allure.addAttachment("Последний HTTP-ответ", "text/plain", details, "txt");
        }
    }
}
