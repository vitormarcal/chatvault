package dev.marcal.chatvault

import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class VersionLogger(
    @Value("\${chatvault.version}") private val appVersion: String,
) {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    @PostConstruct
    fun logVersion() {
        logger.info("Starting ChatVault application version: $appVersion")
    }
}
