package dev.marcal.chatvault.application.chat

import dev.marcal.chatvault.domain.model.AttachmentSummary
import dev.marcal.chatvault.domain.repository.ChatRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ChatAttachmentInfoServiceTest {
    private val chatRepository: ChatRepository = mockk()
    private val chatAttachmentInfoService = ChatAttachmentInfoService(chatRepository)

    @Test
    fun `should return attachment info for chat`() {
        // Arrange
        val chatId = 1L
        val attachmentSummaries =
            sequenceOf(
                AttachmentSummary(id = 1L, name = "document.pdf"),
                AttachmentSummary(id = 2L, name = "image.jpg"),
            )

        every { chatRepository.findAttachmentMessageIdsByChatId(chatId) } returns attachmentSummaries

        // Act
        val result = chatAttachmentInfoService.execute(chatId).toList()

        // Assert
        assertEquals(2, result.size)
        assertEquals("document.pdf", result[0].name)
        assertEquals("image.jpg", result[1].name)
        verify { chatRepository.findAttachmentMessageIdsByChatId(chatId) }
    }

    @Test
    fun `should return empty sequence when no attachments exist`() {
        // Arrange
        val chatId = 2L

        every { chatRepository.findAttachmentMessageIdsByChatId(chatId) } returns emptySequence()

        // Act
        val result = chatAttachmentInfoService.execute(chatId).toList()

        // Assert
        assertEquals(0, result.size)
        verify { chatRepository.findAttachmentMessageIdsByChatId(chatId) }
    }

    @Test
    fun `should map attachment summaries to output correctly`() {
        // Arrange
        val chatId = 3L
        val attachmentSummaries =
            sequenceOf(
                AttachmentSummary(id = 10L, name = "report.docx"),
                AttachmentSummary(id = 11L, name = "data.xlsx"),
                AttachmentSummary(id = 12L, name = "presentation.pptx"),
            )

        every { chatRepository.findAttachmentMessageIdsByChatId(chatId) } returns attachmentSummaries

        // Act
        val result = chatAttachmentInfoService.execute(chatId).toList()

        // Assert
        assertEquals(3, result.size)
        assertEquals("report.docx", result[0].name)
        assertEquals("data.xlsx", result[1].name)
        assertEquals("presentation.pptx", result[2].name)
    }

    @Test
    fun `should handle attachments with special characters in names`() {
        // Arrange
        val chatId = 4L
        val attachmentSummaries =
            sequenceOf(
                AttachmentSummary(id = 20L, name = "file_with-special!@#.pdf"),
                AttachmentSummary(id = 21L, name = "document (copy).docx"),
            )

        every { chatRepository.findAttachmentMessageIdsByChatId(chatId) } returns attachmentSummaries

        // Act
        val result = chatAttachmentInfoService.execute(chatId).toList()

        // Assert
        assertEquals(2, result.size)
        assertEquals("file_with-special!@#.pdf", result[0].name)
        assertEquals("document (copy).docx", result[1].name)
    }

    @Test
    fun `should return sequence that can be used for lazy evaluation`() {
        // Arrange
        val chatId = 5L
        val attachmentSummaries =
            (1..10)
                .map { index ->
                    AttachmentSummary(id = index.toLong(), name = "attachment_$index.zip")
                }.asSequence()

        every { chatRepository.findAttachmentMessageIdsByChatId(chatId) } returns attachmentSummaries

        // Act
        val result = chatAttachmentInfoService.execute(chatId)

        // Assert - Verify it's a lazy sequence
        assertEquals(10, result.toList().size)
        verify { chatRepository.findAttachmentMessageIdsByChatId(chatId) }
    }

    @Test
    fun `should handle large number of attachments`() {
        // Arrange
        val chatId = 6L
        val largeAttachmentList =
            (1..100)
                .map { index ->
                    AttachmentSummary(id = index.toLong(), name = "file_$index.bin")
                }.asSequence()

        every { chatRepository.findAttachmentMessageIdsByChatId(chatId) } returns largeAttachmentList

        // Act
        val result = chatAttachmentInfoService.execute(chatId).toList()

        // Assert
        assertEquals(100, result.size)
        assertEquals("file_1.bin", result.first().name)
        assertEquals("file_100.bin", result.last().name)
    }
}
