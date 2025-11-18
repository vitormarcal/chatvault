package dev.marcal.chatvault.application.chat

import dev.marcal.chatvault.api.dto.mapper.toOutput
import dev.marcal.chatvault.api.dto.output.AttachmentInfoOutput
import dev.marcal.chatvault.domain.repository.ChatRepository
import org.springframework.stereotype.Service

@Service
class ChatAttachmentInfoService(
    private val chatRepository: ChatRepository,
) {
    fun execute(chatId: Long): Sequence<AttachmentInfoOutput> =
        chatRepository
            .findAttachmentMessageIdsByChatId(chatId)
            .map { it.toOutput() }
}
