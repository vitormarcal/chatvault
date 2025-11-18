package dev.marcal.chatvault.application.chat

import dev.marcal.chatvault.api.dto.mapper.toOutput
import dev.marcal.chatvault.api.dto.output.ChatLastMessageOutput
import dev.marcal.chatvault.domain.repository.ChatRepository
import org.springframework.stereotype.Service

@Service
class ChatListerService(
    private val chatRepository: ChatRepository,
) {
    fun execute(): List<ChatLastMessageOutput> =
        chatRepository
            .findAllChatsWithLastMessage()
            .map { it.toOutput() }
            .toList()
}
