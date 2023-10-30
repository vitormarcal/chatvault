package dev.marcal.chatvault.app_service.wpp_legacy_service

import dev.marcal.chatvault.app_service.wpp_legacy_service.dto.ChatDTO
import dev.marcal.chatvault.app_service.wpp_legacy_service.dto.MessageDTO
import dev.marcal.chatvault.app_service.wpp_legacy_service.dto.WppChatResponse
import reactor.core.publisher.Mono
import java.io.InputStream

interface WppLegacyService {
    fun getAllChats(): Mono<WppChatResponse<ChatDTO>>
    fun getMessagesByChatId(chatId: Long, offset: Int, limit: Int): Mono<WppChatResponse<MessageDTO>>
    fun getAttachmentsByMessageId(messageId: String): Mono<InputStream>
}