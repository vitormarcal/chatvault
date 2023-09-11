package dev.marcal.chatvault.usecase

import dev.marcal.chatvault.app_service.WppLegacyService
import dev.marcal.chatvault.app_service.dto.ChatDTO
import dev.marcal.chatvault.app_service.dto.MessageDTO
import dev.marcal.chatvault.app_service.dto.WppChatResponse
import dev.marcal.chatvault.service.ChatLegacyImporter
import dev.marcal.chatvault.service.NewMessage
import dev.marcal.chatvault.service.input.NewAttachmentInput
import dev.marcal.chatvault.service.input.NewMessageInput
import dev.marcal.chatvault.service.input.NewMessagePayloadInput
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class ChatLegacyImporterUseCase(
    private val wppLegacyService: WppLegacyService,
    private val newMessage: NewMessage
) : ChatLegacyImporter {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    override fun execute() {
        getAllChats()
            .flatMap { chat -> findMessagesByChat(chat)
                                .map { response -> toMessagePayloadInput(chat, response) }
            }.subscribe { input ->
                newMessage.execute(input)
            }

    }

    private fun getAllChats() = wppLegacyService.getAllChats()
        .doOnNext {
            logger.info("Found ${it.count} chats")
        }.flatMapIterable { chatResponse -> chatResponse.data }

    private fun findMessagesByChat(chat: ChatDTO) =
        wppLegacyService.getMessagesByChatId(chatId = chat.id, offset = 0, 100)
            .doOnNext { logger.info("Found ${it.count} messages") }

    private fun toMessagePayloadInput(
        chat: ChatDTO,
        response: WppChatResponse<MessageDTO>
    ) = NewMessagePayloadInput(
        chatId = chat.id,
        messages = response.data.map { message ->
            NewMessageInput(
                chatId = chat.id,
                authorName = message.author ?: "",
                createdAt = LocalDateTime.now(),
                content = message.content,
                attachment = message.attachmentName?.let { NewAttachmentInput(name = it, content = "") }
            )
        }
    )
}