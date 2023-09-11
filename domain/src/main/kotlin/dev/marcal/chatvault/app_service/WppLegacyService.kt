package dev.marcal.chatvault.app_service

import dev.marcal.chatvault.app_service.dto.ChatDTO
import dev.marcal.chatvault.app_service.dto.MessageDTO
import dev.marcal.chatvault.app_service.dto.WppChatResponse
import reactor.core.publisher.Mono

interface WppLegacyService {
    fun getAllChats(): Mono<WppChatResponse<ChatDTO>>
    fun getMessagesByChatId(chatId: Long, offset: Int, limit: Int): Mono<WppChatResponse<MessageDTO>>
}