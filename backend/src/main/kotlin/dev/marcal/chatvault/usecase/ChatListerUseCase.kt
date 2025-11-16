package dev.marcal.chatvault.usecase

import dev.marcal.chatvault.domain.repository.ChatRepository
import dev.marcal.chatvault.ioboundary.output.ChatLastMessageOutput
import dev.marcal.chatvault.service.ChatLister
import dev.marcal.chatvault.usecase.mapper.toOutput
import org.springframework.stereotype.Service

@Service
class ChatListerUseCase(
    private val chatRepository: ChatRepository,
) : ChatLister {
    override fun execute(): List<ChatLastMessageOutput> =
        chatRepository
            .findAllChatsWithLastMessage()
            .map { it.toOutput() }
            .toList()
}
