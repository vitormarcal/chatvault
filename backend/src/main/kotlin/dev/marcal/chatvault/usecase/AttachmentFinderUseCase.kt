package dev.marcal.chatvault.usecase

import dev.marcal.chatvault.domain.bucketservice.BucketService
import dev.marcal.chatvault.domain.repository.ChatRepository
import dev.marcal.chatvault.ioboundary.input.AttachmentCriteriaInput
import dev.marcal.chatvault.ioboundary.output.exceptions.AttachmentNotFoundException
import dev.marcal.chatvault.service.AttachmentFinder
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service

@Service
class AttachmentFinderUseCase(
    private val bucketService: BucketService,
    private val chatRepository: ChatRepository,
) : AttachmentFinder {
    override fun execute(criteriaInput: AttachmentCriteriaInput): Resource {
        val message =
            chatRepository.findMessageBy(chatId = criteriaInput.chatId, messageId = criteriaInput.messageId)
                ?: throw AttachmentNotFoundException()

        val bucketFile =
            message.content.attachment?.toBucketFile()
                ?: throw AttachmentNotFoundException("the message exists but there are no attachments linked to it")

        return bucketService.loadFileAsResource(bucketFile)
    }
}
