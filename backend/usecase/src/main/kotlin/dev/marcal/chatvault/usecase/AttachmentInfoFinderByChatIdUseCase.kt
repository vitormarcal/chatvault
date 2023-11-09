package dev.marcal.chatvault.usecase

import dev.marcal.chatvault.in_out_boundary.output.AttachmentInfoOutput
import dev.marcal.chatvault.repository.ChatRepository
import dev.marcal.chatvault.service.AttachmentInfoFinderByChatId
import org.springframework.stereotype.Service

@Service
class AttachmentInfoFinderByChatIdUseCase(
    private val chatRepository: ChatRepository
) : AttachmentInfoFinderByChatId {
    override fun execute(chatId: Long): Sequence<AttachmentInfoOutput> {
        return chatRepository.findAttachmentMessageIdsByChatId(chatId)
    }
}