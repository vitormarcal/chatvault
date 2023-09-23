package dev.marcal.chatvault.usecase

import dev.marcal.chatvault.app_service.bucket_service.BucketService
import dev.marcal.chatvault.in_out_boundary.input.AttachmentCriteriaInput
import dev.marcal.chatvault.in_out_boundary.output.exceptions.AttachmentFinderException
import dev.marcal.chatvault.in_out_boundary.output.exceptions.AttachmentNotFoundException
import dev.marcal.chatvault.repository.ChatRepository
import dev.marcal.chatvault.service.AttachmentFinder
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service

@Service
class AttachmentFinderUseCase(
    private val bucketService: BucketService,
    private val chatRepository: ChatRepository
): AttachmentFinder {
    override fun execute(criteriaInput: AttachmentCriteriaInput): Resource {
        val message =
            chatRepository.findMessageBy(chatId = criteriaInput.chatId, messageId = criteriaInput.messageId) ?: throw AttachmentNotFoundException()

        val bucketFile = requireNotNull(message.content.attachment).toBucketFile()

        return bucketService.loadFileAsResource(bucketFile)

    }
}