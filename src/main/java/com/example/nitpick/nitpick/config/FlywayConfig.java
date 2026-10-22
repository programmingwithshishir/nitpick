package com.example.nitpick.nitpick.config;

import org.flywaydb.core.Flyway;
import org.springframework.boot.flyway.autoconfigure.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Flyway configuration that performs a full clean + migrate on every startup.
 * This ensures the database is always reset to a known state (dev/demo mode).
 *
 * Requires spring.flyway.clean-disabled=false in application.properties.
 */
@Configuration
public class FlywayConfig {

    @Bean
    public FlywayMigrationStrategy cleanMigrateStrategy() {
        return flyway -> {
            flyway.clean();   // Drop all objects in the configured schemas
            flyway.migrate(); // Re-run all migrations from scratch
        };
    }
}
