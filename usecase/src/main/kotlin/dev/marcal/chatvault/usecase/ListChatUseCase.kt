package dev.marcal.chatvault.usecase

import dev.marcal.chatvault.repository.ChatRepository
import dev.marcal.chatvault.service.ListChat
import dev.marcal.chatvault.service.output.ChatLastMessageOutput
import dev.marcal.chatvault.usecase.mapper.toOutput
import org.springframework.stereotype.Service

@Service
class ListChatUseCase(
    private val chatRepository: ChatRepository
): ListChat {
    override fun execute(): List<ChatLastMessageOutput> {
        return chatRepository.findAllChatsWithLastMessage()
            .map { it.toOutput() }
            .toList()
    }
}