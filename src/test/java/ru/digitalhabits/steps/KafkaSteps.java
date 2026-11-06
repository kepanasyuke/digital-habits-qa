package ru.digitalhabits.steps;

import io.cucumber.java.ru.Тогда;
import ru.digitalhabits.support.DateSupport;
import ru.digitalhabits.support.KafkaVerifier;
import ru.digitalhabits.support.ScenarioContext;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class KafkaSteps {

    private final KafkaVerifier kafka;
    private final ScenarioContext ctx;

    public KafkaSteps(KafkaVerifier kafka, ScenarioContext ctx) {
        this.kafka = kafka;
        this.ctx = ctx;
    }

    @Тогда("событие {string} опубликовано в топик с habitId привычки")
    public void eventPublished(String eventType) {
        Map<String, Object> event = kafka.awaitEvent(ctx.getHabitId().toString(), eventType);
        assertThat(event.get("eventId")).as("eventId в событии").isNotNull();
    }

    @Тогда("в событии поле {string} соответствует созданному пользователю")
    public void eventFieldMatchesUser(String field) {
        assertThat(kafka.lastEvent().get(field))
                .as("Поле %s события", field)
                .isEqualTo(ctx.getUserId().toString());
    }

    @Тогда("в событии поле {string} равно дате сегодня")
    public void eventFieldEqualsToday(String field) {
        assertThat(kafka.lastEvent().get(field))
                .as("Поле %s события", field)
                .isEqualTo(DateSupport.isoToday());
    }
}
