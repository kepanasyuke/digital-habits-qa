package ru.digitalhabits.steps;

import io.cucumber.java.ru.Когда;
import ru.digitalhabits.support.ApiClient;
import ru.digitalhabits.support.DateSupport;
import ru.digitalhabits.support.ScenarioContext;

import java.util.Map;

public class CheckinSteps {

    private final ApiClient api;
    private final ScenarioContext ctx;

    public CheckinSteps(ApiClient api, ScenarioContext ctx) {
        this.api = api;
        this.ctx = ctx;
    }

    @Когда("я отмечаю выполнение привычки сегодня")
    public void checkinToday() {
        api.post("/habits/" + ctx.getHabitId() + "/checkins",
                Map.of("date", DateSupport.isoToday()));
    }

    @Когда("я отмечаю выполнение привычки за дату {string}")
    public void checkinForDate(String dateToken) {
        api.post("/habits/" + ctx.getHabitId() + "/checkins",
                Map.of("date", DateSupport.resolve(dateToken).toString()));
    }

    @Когда("я повторно отмечаю выполнение привычки сегодня")
    public void checkinTodayAgain() {
        api.post("/habits/" + ctx.getHabitId() + "/checkins",
                Map.of("date", DateSupport.isoToday()));
    }

    @Когда("я запрашиваю статистику привычки")
    public void requestStats() {
        api.get("/habits/" + ctx.getHabitId() + "/stats");
    }
}
