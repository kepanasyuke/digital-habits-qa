package ru.digitalhabits.support;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import ru.digitalhabits.HabitsApplication;

@CucumberContextConfiguration
@SpringBootTest(classes = HabitsApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = {PostgresInitializer.class, KafkaInitializer.class})
@Import(TestSupportConfig.class)
public class CucumberSpringConfiguration {
}
