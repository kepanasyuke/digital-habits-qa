package ru.digitalhabits.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.kafka.test.EmbeddedKafkaKraftBroker;
import org.springframework.test.context.support.TestPropertySourceUtils;

public class KafkaInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final Logger log = LoggerFactory.getLogger(KafkaInitializer.class);

    public static final String TOPIC = "habit-events";

    private static EmbeddedKafkaKraftBroker broker;

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        if (!Boolean.getBoolean("kafka.enabled")) {
            log.info("Kafka отключена: сценарии с @kafka не запускаются");
            return;
        }
        String brokers = start();
        log.info("Встроенный Kafka-брокер поднят: {}", brokers);

        TestPropertySourceUtils.addInlinedPropertiesToEnvironment(applicationContext,
                "spring.kafka.bootstrap-servers=" + brokers,
                "app.events.enabled=true",
                "app.events.topic=" + TOPIC);
    }

    private static synchronized String start() {
        if (broker == null) {
            broker = new EmbeddedKafkaKraftBroker(1, 1, TOPIC);
            broker.afterPropertiesSet();
        }
        return broker.getBrokersAsString();
    }
}
