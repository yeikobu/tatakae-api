package fit.tatakae.infrastructure.persistence.adapter;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

// Adapters are verified against the same PostgreSQL engine the application runs on, never an embedded database.
public abstract class PostgresIntegrationTest {

    protected static final Instant NOW = Instant.parse("2026-08-28T10:00:00Z");
    protected static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);

    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @TestConfiguration
    public static class TestClockConfiguration {
        @Bean
        public Clock clock() {
            return CLOCK;
        }
    }
}
