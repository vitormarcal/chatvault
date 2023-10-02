package dev.marcal.chatvault.persistence.config

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource

@Configuration
@PropertySource("classpath:persistence.properties")
class PostgresConfig {
}