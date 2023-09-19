package dev.marcal.chatvault.usecase

import dev.marcal.chatvault.repository.ChatRepository
import dev.marcal.chatvault.service.ChatLister
import dev.marcal.chatvault.in_out_boundary.output.ChatLastMessageOutput
import dev.marcal.chatvault.usecase.mapper.toOutput
import org.springframework.stereotype.Service

@Service
class ChatListerUseCase(
    private val chatRepository: ChatRepository
): ChatLister {
    override fun execute(): List<ChatLastMessageOutput> {
        return chatRepository.findAllChatsWithLastMessage()
            .map { it.toOutput() }
            .toList()
    }
}