package dev.marcal.chatvault.app_service.wpp_legacy_service

import dev.marcal.chatvault.app_service.dto.ChatDTO
import dev.marcal.chatvault.app_service.dto.MessageDTO
import dev.marcal.chatvault.app_service.dto.WppChatResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.PropertySource
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Service
@PropertySource("classpath:appservice.properties")
class WppLegacyServiceImpl(
    private val webClient: WebClient,
    private val wppLegacyAuthenticatorService: WppLegacyAuthenticatorService,
    @Value("\${app.legacy.all-chats}") private val allChatsUrl: String,
    @Value("\${app.legacy.messages-by-chat-id}") private val messagesByChatIdUrl: String,
    @Value("\${app.legacy.attachments-by-message-id}") private val attachmentsByMessageIdUrl: String
): WppLegacyService {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    override fun getAllChats(): Mono<WppChatResponse<ChatDTO>> {
        return wppLegacyAuthenticatorService.getToken().flatMap { token ->
            webClient
                .get()
                .uri(allChatsUrl)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .retrieve()
                .bodyToMono(object : ParameterizedTypeReference<WppChatResponse<ChatDTO>>() {})
        }
    }

    override fun getMessagesByChatId(chatId: Long, offset: Int, limit: Int): Mono<WppChatResponse<MessageDTO>> {
        return wppLegacyAuthenticatorService.getToken().flatMap { token ->
            webClient
                .get()
                .uri(
                    messagesByChatIdUrl.replace("{chatId}", chatId.toString())
                        .replace("{offset}", offset.toString())
                        .replace("{limit}", limit.toString())
                )
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .retrieve()
                .bodyToMono(object : ParameterizedTypeReference<WppChatResponse<MessageDTO>>() {})
                .retry(3)
                .doOnError {
                    logger.error("Fail to get messages chatId=$chatId, offset=$offset, limit=$limit", it)
                }
        }
    }

    override fun getAttachmentsByMessageId(messageId: String): Mono<ByteArray> {
        return webClient
            .get()
            .uri(
                attachmentsByMessageIdUrl.replace("{messageId}", messageId.toString())
            )
            .retrieve()
            .bodyToMono(ByteArray::class.java)
            .retry(3)
            .doOnError {
                logger.error("Fail to get attachments info messageId=$messageId", it)
            }
    }

}