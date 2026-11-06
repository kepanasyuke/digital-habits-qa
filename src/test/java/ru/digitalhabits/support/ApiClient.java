package ru.digitalhabits.support;

import io.cucumber.spring.ScenarioScope;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.Filter;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

import static io.restassured.RestAssured.given;

@Component
@ScenarioScope
public class ApiClient {

    private static final Filter ALLURE = new AllureHttpFilter();

    private final RequestSpecification spec;
    private final String baseUri;
    private Response lastResponse;

    public ApiClient(@Value("${local.server.port}") int port) {
        this.baseUri = "http://localhost:" + port;
        this.spec = new RequestSpecBuilder()
                .setBaseUri(baseUri)
                .setBasePath("/api/v1")
                .setContentType(ContentType.JSON)
                .addFilter(ALLURE)
                .build();
    }

    public Response get(String path) {
        lastResponse = given().spec(spec).when().get(path);
        return lastResponse;
    }

    public Response post(String path, Object body) {
        lastResponse = given().spec(spec).body(body).when().post(path);
        return lastResponse;
    }

    public Response patch(String path, Object body) {
        lastResponse = given().spec(spec).body(body).when().patch(path);
        return lastResponse;
    }

    public Response delete(String path) {
        lastResponse = given().spec(spec).when().delete(path);
        return lastResponse;
    }

    public Response health() {
        return given().baseUri(baseUri)
                .when().get("/actuator/health");
    }

    public Map<String, Object> createUser(String email, String displayName) {
        return post("/users", Map.of("email", email, "displayName", displayName)).as(Map.class);
    }

    public Map<String, Object> createHabit(Object userId, String title, int targetPerWeek) {
        return post("/habits", Map.of("userId", String.valueOf(userId), "title", title, "targetPerWeek", targetPerWeek))
                .as(Map.class);
    }

    public Map<String, Object> checkin(Object habitId, String isoDate) {
        return post("/habits/" + habitId + "/checkins", Map.of("date", isoDate)).as(Map.class);
    }

    public Response lastResponse() {
        return lastResponse;
    }
}
