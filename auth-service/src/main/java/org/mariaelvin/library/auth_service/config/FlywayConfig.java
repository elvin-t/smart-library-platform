package org.mariaelvin.library.auth_service.config;



import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
@Slf4j
public class FlywayConfig {

    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy(Environment environment) {

        return flyway -> {

            boolean isLocalProfile = environment.matchesProfiles("local");

            if (!isLocalProfile) {
                log.info("[FlywayConfig] Skipping Flyway migration. Active profile is not 'local'.");
                return;
            }

            log.info("[FlywayConfig] Running schema migration for 'local' profile only.");

            Flyway.configure()
                    .dataSource(flyway.getConfiguration().getDataSource())
                    .locations("classpath:db/migration/schema")
                    .baselineOnMigrate(true)
                    .load()
                    .migrate();
        };
    }
}

