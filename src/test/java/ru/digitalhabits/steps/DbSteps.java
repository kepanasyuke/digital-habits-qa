package ru.digitalhabits.steps;

import io.cucumber.java.ru.Тогда;
import ru.digitalhabits.support.DateSupport;
import ru.digitalhabits.support.DbClient;
import ru.digitalhabits.support.ScenarioContext;

import static org.assertj.core.api.Assertions.assertThat;

public class DbSteps {

    private final DbClient db;
    private final ScenarioContext ctx;

    public DbSteps(DbClient db, ScenarioContext ctx) {
        this.db = db;
        this.ctx = ctx;
    }

    @Тогда("в базе данных ровно {int} отметок о выполнении")
    public void checkinsCount(int expected) {
        assertThat(db.countCheckins(ctx.getHabitId()))
                .as("Количество строк в checkins по привычке %s", ctx.getHabitId())
                .isEqualTo(expected);
    }

    @Тогда("в базе данных есть отметка за дату {string}")
    public void checkinExistsForDate(String dateToken) {
        assertThat(db.countCheckinsByDate(ctx.getHabitId(), DateSupport.resolve(dateToken)))
                .as("Ожидалась отметка за %s", dateToken)
                .isEqualTo(1);
    }

    @Тогда("в базе данных нет отметок по привычке")
    public void noCheckins() {
        assertThat(db.countCheckins(ctx.getHabitId())).isZero();
        assertThat(db.countOrphanCheckins())
                .as("После удаления привычки не должно остаться висячих отметок")
                .isZero();
    }

    @Тогда("в базе данных привычка имеет статус {string}")
    public void habitHasStatus(String expected) {
        assertThat(db.habitStatus(ctx.getHabitId()))
                .as("Статус привычки в таблице habits")
                .isEqualTo(expected);
    }

    @Тогда("в базе данных не появилось привычек")
    public void noHabitsCreated() {
        assertThat(db.countHabits()).isZero();
    }
}
