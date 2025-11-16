package dev.marcal.chatvault.email.config

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.dsl.IntegrationFlow
import org.springframework.integration.dsl.MessageChannels
import org.springframework.integration.dsl.PollerFactory
import org.springframework.integration.dsl.SourcePollingChannelAdapterSpec
import org.springframework.integration.mail.dsl.Mail
import org.springframework.integration.support.PropertiesBuilder

@Configuration
@ConditionalOnProperty(prefix = "chatvault.email", name = ["enabled"], havingValue = "true", matchIfMissing = true)
class EmailHandlerConfig(
    @Value("\${chatvault.email.host}") private val host: String,
    @Value("\${chatvault.email.port}") private val port: String,
    @Value("\${chatvault.email.username}") private val username: String,
    @Value("\${chatvault.email.password}") private val password: String,
    @Value("\${chatvault.email.fixed-delay-mlss}") private val fixedDelay: Long,
    @Value("\${chatvault.email.debug}") private val emailDebug: Boolean,
    @Value("\${chatvault.email.subject-starts-with}") private val subjectStartsWithList: List<String>,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @Bean
    fun imapMailFlow(): IntegrationFlow {
        logger.info("creating imapMailFlow integration flow receive emails")
        return IntegrationFlow
            .from(
                Mail
                    .imapInboundAdapter("imap://$username:$password@$host:$port/INBOX")
                    .selector { mimeMessage ->
                        subjectStartsWithList.any { mimeMessage.subject.startsWith(it, ignoreCase = true) }
                    }.userFlag("chat-vault")
                    .autoCloseFolder(false)
                    .javaMailProperties { p: PropertiesBuilder ->
                        p.put("mail.debug", emailDebug)
                        p.put("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
                        p.put("mail.store.protocol", "imap")
                        p.put("mail.imap.socketFactory.fallback", "false")
                    },
            ) { e: SourcePollingChannelAdapterSpec ->
                e
                    .autoStartup(true)
                    .poller { p: PollerFactory -> p.fixedDelay(fixedDelay) }
            }.channel(MessageChannels.queue("imapChannel"))
            .get()
    }
}
