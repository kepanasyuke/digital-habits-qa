package ru.digitalhabits.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.digitalhabits.domain.Checkin;
import ru.digitalhabits.domain.Habit;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class HabitEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(HabitEventPublisher.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final boolean enabled;
    private final String topic;

    public HabitEventPublisher(KafkaTemplate<String, String> kafkaTemplate,
                               ObjectMapper objectMapper,
                               @Value("${app.events.enabled:false}") boolean enabled,
                               @Value("${app.events.topic:habit-events}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.enabled = enabled;
        this.topic = topic;
    }

    public void checkinCreated(Habit habit, Checkin checkin) {
        if (!enabled) {
            log.debug("Публикация событий выключена, событие по чек-ину {} пропущено", checkin.getId());
            return;
        }
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("eventId", UUID.randomUUID().toString());
        payload.put("eventType", "HABIT_CHECKIN_CREATED");
        payload.put("habitId", habit.getId().toString());
        payload.put("userId", habit.getUserId().toString());
        payload.put("checkinDate", checkin.getCheckinDate().toString());
        payload.put("occurredAt", Instant.now().toString());

        try {
            String json = objectMapper.writeValueAsString(payload);
            kafkaTemplate.send(topic, habit.getId().toString(), json);
            log.info("Событие HABIT_CHECKIN_CREATED отправлено в топик {}: {}", topic, json);
        } catch (Exception e) {
            throw new IllegalStateException("Не удалось опубликовать событие чек-ина " + checkin.getId(), e);
        }
    }
}
