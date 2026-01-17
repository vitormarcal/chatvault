package dev.marcal.chatvault.application.chat

import dev.marcal.chatvault.api.web.exception.ChatNotFoundException
import dev.marcal.chatvault.application.message.MessageFinderService
import dev.marcal.chatvault.domain.bucket.BucketService
import dev.marcal.chatvault.domain.repository.ChatRepository
import dev.marcal.chatvault.shared.chatBucketInfoWith
import dev.marcal.chatvault.shared.chatLastMessageWith
import dev.marcal.chatvault.shared.messageOutputWith
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.core.io.Resource
import java.time.LocalDateTime
import kotlin.test.assertEquals

class ChatExportServiceTest {
    private val bucketService: BucketService = mockk()
    private val chatRepository: ChatRepository = mockk()
    private val messageFinderService: MessageFinderService = mockk()
    private val chatExportService = ChatExportService(bucketService, chatRepository, messageFinderService)

    @Test
    fun `should export single chat successfully`() {
        // Arrange
        val chatId = 1L
        val chatBucketInfo = chatBucketInfoWith(chatId = chatId)
        val mockResource = mockk<Resource>()

        every { chatRepository.findChatBucketInfoByChatId(chatId) } returns chatBucketInfo
        every { messageFinderService.execute(chatId) } returns sequenceOf()
        every { bucketService.saveTextToBucket(any(), any()) } returns Unit
        every { bucketService.loadBucketAsZip(chatBucketInfo.bucket.path) } returns mockResource

        // Act
        val result = chatExportService.execute(chatId)

        // Assert
        assertEquals(mockResource, result)
        verify { bucketService.loadBucketAsZip(chatBucketInfo.bucket.path) }
    }

    @Test
    fun `should format messages correctly for export`() {
        // Arrange
        val chatId = 2L
        val chatBucketInfo = chatBucketInfoWith(chatId = chatId)
        val createdAt = LocalDateTime.of(2023, 8, 15, 18, 30)
        val messageOutput = messageOutputWith(
            author = "John",
            authorType = "USER",
            content = "Hello world",
            createdAt = createdAt,
        )
        val mockResource = mockk<Resource>()

        every { chatRepository.findChatBucketInfoByChatId(chatId) } returns chatBucketInfo
        every { messageFinderService.execute(chatId) } returns sequenceOf(messageOutput)
        every { bucketService.saveTextToBucket(any(), any()) } returns Unit
        every { bucketService.loadBucketAsZip(chatBucketInfo.bucket.path) } returns mockResource

        // Act
        chatExportService.execute(chatId)

        // Assert
        verify { bucketService.saveTextToBucket(any(), any()) }
    }

    @Test
    fun `should handle system messages without author name`() {
        // Arrange
        val chatId = 3L
        val chatBucketInfo = chatBucketInfoWith(chatId = chatId)
        val systemMessage = messageOutputWith(
            author = "",
            authorType = "SYSTEM",
            content = "System notification",
            createdAt = LocalDateTime.of(2023, 1, 1, 12, 0),
        )
        val mockResource = mockk<Resource>()

        every { chatRepository.findChatBucketInfoByChatId(chatId) } returns chatBucketInfo
        every { messageFinderService.execute(chatId) } returns sequenceOf(systemMessage)
        every { bucketService.saveTextToBucket(any(), any()) } returns Unit
        every { bucketService.loadBucketAsZip(chatBucketInfo.bucket.path) } returns mockResource

        // Act
        chatExportService.execute(chatId)

        // Assert
        verify { bucketService.saveTextToBucket(any(), any()) }
    }

    @Test
    fun `should throw ChatNotFoundException when chat does not exist`() {
        // Arrange
        val nonExistentChatId = 999L

        every { chatRepository.findChatBucketInfoByChatId(nonExistentChatId) } returns null

        // Act & Assert
        val exception = assertThrows<ChatNotFoundException> {
            chatExportService.execute(nonExistentChatId)
        }
        assertEquals(
            "The export failed. Chat with id $nonExistentChatId was not found.",
            exception.message,
        )
    }

    @Test
    fun `should export all chats successfully`() {
        // Arrange
        val chat1 = chatLastMessageWith(chatId = 1L)
        val chat2 = chatLastMessageWith(chatId = 2L)
        val mockZipResource = mockk<Resource>()

        every { chatRepository.findAllChatsWithLastMessage() } returns sequenceOf(chat1, chat2)
        every { chatRepository.findChatBucketInfoByChatId(any()) } answers {
            val chatId = args[0] as Long
            chatBucketInfoWith(chatId = chatId)
        }
        every { messageFinderService.execute(any()) } returns sequenceOf()
        every { bucketService.saveTextToBucket(any(), any()) } returns Unit
        every { bucketService.loadBucketListAsZip() } returns mockZipResource

        // Act
        val result = chatExportService.executeAll()

        // Assert
        assertEquals(mockZipResource, result)
        verify(exactly = 2) { messageFinderService.execute(any()) }
        verify { bucketService.loadBucketListAsZip() }
    }

    @Test
    fun `should handle multiple messages in sequence`() {
        // Arrange
        val chatId = 5L
        val chatBucketInfo = chatBucketInfoWith(chatId = chatId)
        val messages = (1..5).map { index ->
            messageOutputWith(
                author = "User",
                content = "Message $index",
                createdAt = LocalDateTime.of(2023, 1, 1, index, 0),
            )
        }
        val mockResource = mockk<Resource>()

        every { chatRepository.findChatBucketInfoByChatId(chatId) } returns chatBucketInfo
        every { messageFinderService.execute(chatId) } returns messages.asSequence()
        every { bucketService.saveTextToBucket(any(), any()) } returns Unit
        every { bucketService.loadBucketAsZip(chatBucketInfo.bucket.path) } returns mockResource

        // Act
        chatExportService.execute(chatId)

        // Assert
        verify { bucketService.saveTextToBucket(any(), any()) }
    }
}
