package dev.marcal.chatvault.usecase

import dev.marcal.chatvault.in_out_boundary.output.MessageOutput
import dev.marcal.chatvault.repository.ChatRepository
import dev.marcal.chatvault.service.MessageFinderByChatId
import dev.marcal.chatvault.usecase.mapper.toOutput
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort.Direction
import org.springframework.stereotype.Service

@Service
class MessageFinderByChatIdUseCase(
    private val chatRepository: ChatRepository
) : MessageFinderByChatId {
    override fun execute(chatId: Long, query: String?, pageable: Pageable): Page<MessageOutput> {
        return chatRepository.findMessagesBy(chatId = chatId, query = query, pageable = pageable)
    }

    override fun execute(chatId: Long): Sequence<MessageOutput> {
        return generateSequence(0) { it + 1 }
            .map { page ->
                val pageable = PageRequest.of(page, 200, Direction.ASC, "id")
                chatRepository.findMessagesBy(chatId = chatId, pageable = pageable)
            }
            .takeWhile { it.hasContent() }
            .flatMap { it.content.asSequence() }
    }
}