package dev.marcal.chatvault.usecase

import dev.marcal.chatvault.domain.repository.ChatRepository
import dev.marcal.chatvault.ioboundary.output.AttachmentInfoOutput
import org.springframework.stereotype.Service

@Service
class AttachmentInfoFinderByChatIdUseCase(
    private val chatRepository: ChatRepository,
) {
    fun execute(chatId: Long): Sequence<AttachmentInfoOutput> = chatRepository.findAttachmentMessageIdsByChatId(chatId)
}
