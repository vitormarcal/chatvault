package dev.marcal.chatvault.email.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource
import org.springframework.integration.dsl.IntegrationFlow
import org.springframework.integration.dsl.MessageChannels
import org.springframework.integration.dsl.PollerFactory
import org.springframework.integration.dsl.SourcePollingChannelAdapterSpec
import org.springframework.integration.mail.dsl.Mail
import org.springframework.integration.support.PropertiesBuilder


@Configuration
@PropertySource("classpath:email.properties")
@ConditionalOnProperty(prefix = "app.email", name = ["enabled"], havingValue = "true", matchIfMissing = true)
class EmailHandlerConfig(
    @Value("\${app.email.host}") private val host: String,
    @Value("\${app.email.port}") private val port: String,
    @Value("\${app.email.username}") private val username: String,
    @Value("\${app.email.password}") private val password: String,
    @Value("\${app.email.fixed-delay-mlss}") private val fixedDelay: Long,
    @Value("\${app.email.subject-starts-with}") private val subjectStartsWithList: List<String>,
) {


    @Bean
    fun imapMailFlow(): IntegrationFlow {
        return IntegrationFlow
            .from(Mail.imapInboundAdapter("imap://${username}:${password}@${host}:${port}/INBOX")
                .selector { mimeMessage ->
                    subjectStartsWithList.any { mimeMessage.subject.startsWith(it, ignoreCase = true)   }
                }
                .userFlag("chat-vault")
                .autoCloseFolder(false)
                .javaMailProperties { p: PropertiesBuilder ->
                    p.put("mail.debug", "true")
                    p.put("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
                    p.put("mail.store.protocol", "imap")
                    p.put("mail.imap.socketFactory.fallback", "false")
                }
            ) { e: SourcePollingChannelAdapterSpec ->
                e.autoStartup(true)
                    .poller { p: PollerFactory -> p.fixedDelay(fixedDelay) }
            }
            .channel(MessageChannels.queue("imapChannel"))
            .get()
    }

}