package dev.marcal.chatvault.domain.config

import dev.marcal.chatvault.domain.model.MessageParser
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class UseCaseConfig(
    @Value("\${chatvault.msgparser.dateformat}") private val localDateTimePattern: String?,
) {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    @Bean
    fun messageParser(): MessageParser {
        val customPattern =
            localDateTimePattern
                ?.takeIf {
                    it.isNotBlank()
                }?.also { logger.info("Custom local datetime pattern $localDateTimePattern message parser") }
        return MessageParser(customPattern)
    }
}
