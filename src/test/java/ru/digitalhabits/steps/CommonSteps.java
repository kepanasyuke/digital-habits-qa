package ru.digitalhabits.steps;

import io.cucumber.java.ru.Дано;
import io.cucumber.java.ru.Когда;
import io.cucumber.java.ru.Тогда;
import io.restassured.response.Response;
import ru.digitalhabits.support.ApiClient;
import ru.digitalhabits.support.DateSupport;
import ru.digitalhabits.support.ScenarioContext;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class CommonSteps {

    private final ApiClient api;
    private final ScenarioContext ctx;

    public CommonSteps(ApiClient api, ScenarioContext ctx) {
        this.api = api;
        this.ctx = ctx;
    }

    @Дано("API привычек доступно")
    public void apiIsAvailable() {
        Response health = api.health();
        assertThat(health.statusCode())
                .as("Health-check сервиса: %s", health.asString())
                .isEqualTo(200);
    }

    @Когда("я создаю пользователя {string} с именем {string}")
    public void createUser(String email, String displayName) {
        Response response = api.post("/users", Map.of("email", email, "displayName", displayName));
        ctx.setUserEmail(email);
        if (response.statusCode() == 201) {
            ctx.setUserId(UUID.fromString(response.jsonPath().getString("id")));
        }
    }

    @Когда("я отправляю запрос на создание пользователя с email {string}")
    public void createUserWithEmail(String email) {
        api.post("/users", Map.of("email", email, "displayName", "Пользователь"));
    }

    @Когда("я отправляю запрос на создание пользователя с email {string} и пустым именем")
    public void createUserWithEmptyName(String email) {
        api.post("/users", Map.of("email", email, "displayName", ""));
    }

    @Когда("я создаю привычку {string} с целью {int} раз в неделю")
    public void createHabit(String title, int targetPerWeek) {
        Response response = api.post("/habits", Map.of(
                "userId", String.valueOf(ctx.getUserId()),
                "title", title,
                "targetPerWeek", targetPerWeek));
        ctx.setHabitTitle(title);
        if (response.statusCode() == 201) {
            ctx.setHabitId(UUID.fromString(response.jsonPath().getString("id")));
        }
    }

    @Когда("я создаю привычку для несуществующего пользователя")
    public void createHabitForUnknownUser() {
        api.post("/habits", Map.of(
                "userId", UUID.randomUUID().toString(),
                "title", "Призрак",
                "targetPerWeek", 3));
    }

    @Когда("я запрашиваю привычку по идентификатору")
    public void getHabitById() {
        api.get("/habits/" + ctx.getHabitId());
    }

    @Когда("я запрашиваю привычку по несуществующему идентификатору")
    public void getHabitByUnknownId() {
        api.get("/habits/" + UUID.randomUUID());
    }

    @Когда("я запрашиваю привычки пользователя")
    public void getUserHabits() {
        api.get("/users/" + ctx.getUserId() + "/habits");
    }

    @Когда("я меняю статус привычки на {string}")
    public void changeStatus(String status) {
        api.patch("/habits/" + ctx.getHabitId() + "/status", Map.of("status", status));
    }

    @Когда("я удаляю привычку")
    public void deleteHabit() {
        api.delete("/habits/" + ctx.getHabitId());
    }

    @Тогда("ответ имеет статус {int}")
    public void responseHasStatus(int expected) {
        assertThat(api.lastResponse().statusCode())
                .as("Статус ответа. Тело: %s", api.lastResponse().asString())
                .isEqualTo(expected);
    }

    @Тогда("в ответе поле {string} равно {string}")
    public void responseFieldEquals(String path, String expected) {
        Object actual = api.lastResponse().jsonPath().get(path);
        String expectedValue = isDateToken(expected) ? DateSupport.resolve(expected).toString() : expected;
        assertThat(String.valueOf(actual))
                .as("Поле %s в ответе: %s", path, api.lastResponse().asString())
                .isEqualTo(expectedValue);
    }

    @Тогда("в ответе поле {string} равно {число}")
    public void responseFieldEqualsNumber(String path, BigDecimal expected) {
        Object actual = api.lastResponse().jsonPath().get(path);
        assertThat(actual).as("Поле %s в ответе: %s", path, api.lastResponse().asString()).isNotNull();
        assertThat(new BigDecimal(String.valueOf(actual)).compareTo(expected))
                .as("Поле %s: ожидалось %s, получено %s", path, expected, actual)
                .isZero();
    }

    @Тогда("в ответе поле {string} не пустое")
    public void responseFieldNotEmpty(String path) {
        Object actual = api.lastResponse().jsonPath().get(path);
        assertThat(actual).as("Поле %s в ответе: %s", path, api.lastResponse().asString()).isNotNull();
        assertThat(String.valueOf(actual)).isNotBlank();
    }

    @Тогда("размер списка в ответе равен {int}")
    public void responseListSize(int expected) {
        java.util.List<Object> list = api.lastResponse().jsonPath().getList("$");
        assertThat(list).as("Список в ответе: %s", api.lastResponse().asString()).hasSize(expected);
    }

    @Тогда("в ответе поле {string} больше {int}")
    public void responseFieldGreaterThan(String path, int threshold) {
        Number actual = api.lastResponse().jsonPath().get(path);
        assertThat(actual).as("Поле %s в ответе: %s", path, api.lastResponse().asString()).isNotNull();
        assertThat(actual.doubleValue()).isGreaterThan(threshold);
    }

    @Тогда("в ответе поле {string} содержит {string}")
    public void responseFieldContains(String path, String expected) {
        java.util.List<String> values = api.lastResponse().jsonPath().getList(path, String.class);
        assertThat(values).as("Поле %s в ответе: %s", path, api.lastResponse().asString())
                .isNotNull()
                .anyMatch(value -> value.contains(expected));
    }

    @Тогда("привычка не найдена по идентификатору")
    public void habitIsNotFound() {
        Response response = api.get("/habits/" + ctx.getHabitId());
        assertThat(response.statusCode())
                .as("Удалённая привычка не должна читаться. Тело: %s", response.asString())
                .isEqualTo(404);
    }

    private boolean isDateToken(String value) {
        return switch (value.trim().toLowerCase()) {
            case "сегодня", "вчера", "позавчера", "завтра" -> true;
            default -> false;
        };
    }
}
