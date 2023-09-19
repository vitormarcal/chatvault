package dev.marcal.chatvault.usecase

import dev.marcal.chatvault.in_out_boundary.output.MessageOutput
import dev.marcal.chatvault.repository.ChatRepository
import dev.marcal.chatvault.service.MessageFinderByChatId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class MessageFinderByChatIdUseCase(
    private val chatRepository: ChatRepository
): MessageFinderByChatId {
    override fun execute(chatId: Long, pageable: Pageable): Page<MessageOutput> {
        return chatRepository.findMessagesBy(chatId = chatId, pageable)
    }
}