package org.opendatamesh.platform.up.executor.gitlabci.config;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import javax.sql.DataSource;

@Configuration
public class FlywayConfiguration {

    @Value("${odm.executors.gitlab.db-config.default-schema}")
    private String DEFAULT_SCHEMA_NAME;
    @Value("${spring.flyway.locations}")
    private String DB_MIGRATION_DEFAULT;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final DataSource dataSource;

    @Autowired
    @Lazy
    public FlywayConfiguration(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Bean
    Flyway flywayDefault() {
        final String schemaName = DEFAULT_SCHEMA_NAME;
        logger.info("Migrating default schema {}", schemaName);
        final FluentConfiguration configuration = Flyway.configure();
        configuration.dataSource(dataSource);
        configuration.locations(DB_MIGRATION_DEFAULT);
        configuration.schemas(schemaName);
        Flyway flyway = new Flyway(configuration);
        flyway.migrate();
        return flyway;
    }

}