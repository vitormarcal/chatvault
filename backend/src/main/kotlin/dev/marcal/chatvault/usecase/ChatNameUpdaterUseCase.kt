package dev.marcal.chatvault.usecase

import dev.marcal.chatvault.domain.repository.ChatRepository
import dev.marcal.chatvault.service.ChatNameUpdater
import org.springframework.stereotype.Service

@Service
class ChatNameUpdaterUseCase(
    private val chatRepository: ChatRepository,
) : ChatNameUpdater {
    override fun execute(
        chatId: Long,
        chatName: String,
    ) {
        require(chatName.isNotBlank()) { "chatName must not be empty" }
        require(chatRepository.existsByChatId(chatId)) { "chat not found" }

        chatRepository.setChatNameByChatId(chatId, chatName)
    }
}
