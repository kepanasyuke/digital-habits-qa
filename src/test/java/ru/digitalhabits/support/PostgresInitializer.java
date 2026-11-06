package ru.digitalhabits.support;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.support.TestPropertySourceUtils;

import java.io.IOException;

public class PostgresInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final Logger log = LoggerFactory.getLogger(PostgresInitializer.class);

    private static EmbeddedPostgres postgres;

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        EmbeddedPostgres instance = start();
        String jdbcUrl = instance.getJdbcUrl("postgres", "postgres");
        log.info("Тестовый PostgreSQL поднят: {}", jdbcUrl);

        TestPropertySourceUtils.addInlinedPropertiesToEnvironment(applicationContext,
                "spring.datasource.url=" + jdbcUrl,
                "spring.datasource.username=postgres",
                "spring.datasource.password=postgres",
                "spring.datasource.driver-class-name=org.postgresql.Driver");
    }

    private static synchronized EmbeddedPostgres start() {
        if (postgres == null) {
            try {
                postgres = EmbeddedPostgres.builder()
                        .setCleanDataDirectory(true)
                        .start();
            } catch (IOException e) {
                throw new IllegalStateException("Не удалось запустить встроенный PostgreSQL", e);
            }
        }
        return postgres;
    }
}
