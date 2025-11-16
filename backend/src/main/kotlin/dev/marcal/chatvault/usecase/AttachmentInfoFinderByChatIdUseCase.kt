package dev.marcal.chatvault.usecase

import dev.marcal.chatvault.domain.repository.ChatRepository
import dev.marcal.chatvault.ioboundary.output.AttachmentInfoOutput
import dev.marcal.chatvault.service.AttachmentInfoFinderByChatId
import org.springframework.stereotype.Service

@Service
class AttachmentInfoFinderByChatIdUseCase(
    private val chatRepository: ChatRepository,
) : AttachmentInfoFinderByChatId {
    override fun execute(chatId: Long): Sequence<AttachmentInfoOutput> = chatRepository.findAttachmentMessageIdsByChatId(chatId)
}
