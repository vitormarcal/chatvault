package dev.marcal.chatvault.config

import dev.marcal.chatvault.model.MessageParser
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource

@Configuration
@PropertySource("classpath:domain.properties")
class UseCaseConfig(
    @Value("\${chatvault.msgparser.dateformat}")  private val localDateTimePattern: String?
) {


    @Bean
    fun messageParser(): MessageParser {
        val customPattern = localDateTimePattern?.takeIf { it.isNotBlank() }
        return MessageParser(customPattern)
    }
}