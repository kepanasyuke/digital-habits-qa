package ru.digitalhabits.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.spring.ScenarioScope;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@ScenarioScope
public class KafkaVerifier {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String bootstrapServers;
    private final String topic;

    private Map<String, Object> lastEvent;

    public KafkaVerifier(@Value("${spring.kafka.bootstrap-servers:localhost:9092}") String bootstrapServers,
                         @Value("${app.events.topic:habit-events}") String topic) {
        this.bootstrapServers = bootstrapServers;
        this.topic = topic;
    }

    public Map<String, Object> awaitEvent(String habitId, String eventType) {
        Map<String, Object> consumerProps = new HashMap<>();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "qa-" + UUID.randomUUID());
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        long deadline = System.currentTimeMillis() + 20_000;
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps)) {
            consumer.subscribe(List.of(topic));
            while (System.currentTimeMillis() < deadline) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));
                for (ConsumerRecord<String, String> record : records) {
                    Map<String, Object> event = parse(record.value());
                    if (habitId.equals(event.get("habitId")) && eventType.equals(event.get("eventType"))) {
                        lastEvent = event;
                        return event;
                    }
                }
            }
        }
        throw new AssertionError("Событие " + eventType + " по привычке " + habitId
                + " не появилось в топике " + topic + " за 20 секунд");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parse(String value) {
        try {
            return objectMapper.readValue(value, Map.class);
        } catch (Exception e) {
            throw new IllegalStateException("Не удалось разобрать событие: " + value, e);
        }
    }

    public Map<String, Object> lastEvent() {
        return lastEvent;
    }

    public List<String> describeAvailableTopics() {
        return new ArrayList<>(List.of(topic));
    }
}
