package dev.marcal.chatvault.application.chat

import dev.marcal.chatvault.api.dto.input.AttachmentCriteriaInput
import dev.marcal.chatvault.api.web.exception.AttachmentNotFoundException
import dev.marcal.chatvault.domain.bucket.BucketService
import dev.marcal.chatvault.domain.repository.ChatRepository
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service

@Service
class ChatAttachmentService(
    private val bucketService: BucketService,
    private val chatRepository: ChatRepository,
) {
    fun execute(criteriaInput: AttachmentCriteriaInput): Resource {
        val message =
            chatRepository.findMessageBy(chatId = criteriaInput.chatId, messageId = criteriaInput.messageId)
                ?: throw AttachmentNotFoundException()

        val bucketFile =
            message.content.attachment?.toBucketFile()
                ?: throw AttachmentNotFoundException("the message exists but there are no attachments linked to it")

        return bucketService.loadFileAsResource(bucketFile)
    }
}
