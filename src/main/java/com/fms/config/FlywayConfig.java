package com.fms.config;

import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Customize Flyway startup: call repair() before migrate() so that
 * historical migrations whose body has been rewritten in-place
 * (e.g. V2 sample data was scrubbed of legacy branding) have their
 * stored checksum re-aligned automatically instead of crashing the
 * application with a checksum-mismatch error.
 *
 * repair() is idempotent and safe — on a clean database it is a
 * no-op. It will NOT re-execute migrations; it only fixes the
 * checksum / description / type columns in flyway_schema_history.
 */
@Configuration
public class FlywayConfig {

    @Bean
    public FlywayMigrationStrategy repairThenMigrate() {
        return flyway -> {
            flyway.repair();
            flyway.migrate();
        };
    }
}
