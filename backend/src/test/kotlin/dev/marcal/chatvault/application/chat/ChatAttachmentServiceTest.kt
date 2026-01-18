package dev.marcal.chatvault.application.chat

import dev.marcal.chatvault.api.dto.input.AttachmentCriteriaInput
import dev.marcal.chatvault.api.web.exception.AttachmentNotFoundException
import dev.marcal.chatvault.domain.bucket.BucketService
import dev.marcal.chatvault.domain.repository.ChatRepository
import dev.marcal.chatvault.shared.attachmentWith
import dev.marcal.chatvault.shared.messageWith
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.core.io.Resource
import kotlin.test.assertEquals

class ChatAttachmentServiceTest {
    private val bucketService: BucketService = mockk()
    private val chatRepository: ChatRepository = mockk()
    private val chatAttachmentService = ChatAttachmentService(bucketService, chatRepository)

    @Test
    fun `should return attachment resource when found`() {
        // Arrange
        val chatId = 1L
        val messageId = 100L
        val attachment = attachmentWith(name = "document.pdf")
        val message = messageWith(id = messageId, attachment = attachment)
        val mockResource = mockk<Resource>()
        val criteria = AttachmentCriteriaInput(chatId = chatId, messageId = messageId)

        every { chatRepository.findMessageBy(chatId, messageId) } returns message
        every { bucketService.loadFileAsResource(any()) } returns mockResource

        // Act
        val result = chatAttachmentService.execute(criteria)

        // Assert
        assertEquals(mockResource, result)
        verify { bucketService.loadFileAsResource(any()) }
    }

    @Test
    fun `should throw AttachmentNotFoundException when message not found`() {
        // Arrange
        val chatId = 2L
        val messageId = 999L
        val criteria = AttachmentCriteriaInput(chatId = chatId, messageId = messageId)

        every { chatRepository.findMessageBy(chatId, messageId) } returns null

        // Act & Assert
        assertThrows<AttachmentNotFoundException> {
            chatAttachmentService.execute(criteria)
        }
    }

    @Test
    fun `should throw AttachmentNotFoundException when message has no attachment`() {
        // Arrange
        val chatId = 3L
        val messageId = 200L
        val messageWithoutAttachment = messageWith(id = messageId, attachment = null)
        val criteria = AttachmentCriteriaInput(chatId = chatId, messageId = messageId)

        every { chatRepository.findMessageBy(chatId, messageId) } returns messageWithoutAttachment

        // Act & Assert
        val exception =
            assertThrows<AttachmentNotFoundException> {
                chatAttachmentService.execute(criteria)
            }
        assertEquals(
            "the message exists but there are no attachments linked to it",
            exception.message,
        )
    }

    @Test
    fun `should handle different attachment file types`() {
        // Arrange
        val chatId = 4L
        val messageId = 300L
        val attachmentPdf = attachmentWith(name = "report.pdf")
        val messagePdf = messageWith(id = messageId, attachment = attachmentPdf)
        val mockResource = mockk<Resource>()
        val criteria = AttachmentCriteriaInput(chatId = chatId, messageId = messageId)

        every { chatRepository.findMessageBy(chatId, messageId) } returns messagePdf
        every { bucketService.loadFileAsResource(any()) } returns mockResource

        // Act
        chatAttachmentService.execute(criteria)

        // Assert
        verify { bucketService.loadFileAsResource(any()) }
    }

    @Test
    fun `should pass correct chatId and messageId to repository`() {
        // Arrange
        val chatId = 5L
        val messageId = 400L
        val attachment = attachmentWith()
        val message = messageWith(id = messageId, attachment = attachment)
        val mockResource = mockk<Resource>()
        val criteria = AttachmentCriteriaInput(chatId = chatId, messageId = messageId)

        every { chatRepository.findMessageBy(chatId, messageId) } returns message
        every { bucketService.loadFileAsResource(any()) } returns mockResource

        // Act
        chatAttachmentService.execute(criteria)

        // Assert
        verify { chatRepository.findMessageBy(chatId, messageId) }
    }
}
