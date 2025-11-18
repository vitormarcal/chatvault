package dev.marcal.chatvault.application.message

import dev.marcal.chatvault.api.dto.mapper.toOutput
import dev.marcal.chatvault.api.dto.output.MessageOutput
import dev.marcal.chatvault.domain.repository.ChatRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort.Direction
import org.springframework.stereotype.Service

@Service
class MessageFinderService(
    private val chatRepository: ChatRepository,
) {
    fun execute(
        chatId: Long,
        query: String?,
        pageable: Pageable,
    ): Page<MessageOutput> {
        val page = chatRepository.findMessagesBy(chatId = chatId, query = query, pageable = pageable)
        val outputs = page.content.map { it.toOutput() }
        return PageImpl(outputs, pageable, page.totalElements)
    }

    fun execute(chatId: Long): Sequence<MessageOutput> =
        generateSequence(0) { it + 1 }
            .map { page ->
                val p = PageRequest.of(page, 200, org.springframework.data.domain.Sort.Direction.ASC, "id")
                chatRepository.findMessagesBy(chatId = chatId, pageable = p)
            }.takeWhile { it.hasContent() }
            .flatMap { it.content.asSequence() }
            .map { it.toOutput() }
}
