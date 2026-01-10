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
import java.time.LocalDateTime

@Service
class MessageDateFinderService(
    private val chatRepository: ChatRepository,
) {
    fun execute(
        chatId: Long,
        targetDate: LocalDateTime,
        pageSize: Int = 20,
    ): Page<MessageOutput> {
        val pageable = PageRequest.of(0, pageSize, Direction.ASC, "createdAt", "id")
        val page = chatRepository.findMessagesAroundDate(
            chatId = chatId,
            targetDate = targetDate,
            pageable = pageable,
        )
        val outputs = page.content.map { it.toOutput() }
        return PageImpl(outputs, pageable, page.totalElements)
    }
}
