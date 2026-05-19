package com.criptopass.ms.ticket.ticket.config

import org.flywaydb.core.Flyway
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource

/**
 * Manual Flyway configuration for Spring Boot 4.x.
 *
 * Spring Boot 4.0.6 removed FlywayAutoConfiguration from the core autoconfigure module.
 * This configuration manually creates and runs Flyway migrations before JPA initialization.
 */
@Configuration
@ConditionalOnProperty(prefix = "spring.flyway", name = ["enabled"], havingValue = "true", matchIfMissing = true)
class FlywayConfig {

    @Bean(initMethod = "migrate")
    fun flyway(dataSource: DataSource): Flyway {
        return Flyway.configure()
            .dataSource(dataSource)
            .locations("classpath:db/migration")
            .baselineOnMigrate(true)
            .load()
    }
}
