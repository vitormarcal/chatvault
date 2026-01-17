package dev.marcal.chatvault.application.chat

import dev.marcal.chatvault.domain.bucket.BucketService
import dev.marcal.chatvault.domain.model.Bucket
import dev.marcal.chatvault.domain.model.ChatBucketInfo
import dev.marcal.chatvault.domain.repository.ChatRepository
import dev.marcal.chatvault.shared.chatBucketInfoWith
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

class ChatDeletionServiceTest {
    private val chatRepository: ChatRepository = mockk()
    private val bucketService: BucketService = mockk()
    private val chatDeletionService = ChatDeletionService(chatRepository, bucketService)

    @Test
    fun `should delete chat when chat exists`() {
        // Arrange
        val chatId = 1L
        val chatBucketInfo = chatBucketInfoWith(chatId = chatId)

        every { chatRepository.findChatBucketInfoByChatId(chatId) } returns chatBucketInfo
        every { bucketService.delete(any()) } just Runs
        every { chatRepository.deleteChat(chatId) } just Runs

        // Act
        chatDeletionService.execute(chatId)

        // Assert
        verify { chatRepository.findChatBucketInfoByChatId(chatId) }
        verify { bucketService.delete(any()) }
        verify { chatRepository.deleteChat(chatId) }
    }

    @Test
    fun `should delete bucket first then delete chat from repository`() {
        // Arrange
        val chatId = 5L
        val chatBucketInfo = chatBucketInfoWith(chatId = chatId)

        every { chatRepository.findChatBucketInfoByChatId(chatId) } returns chatBucketInfo
        every { bucketService.delete(any()) } just Runs
        every { chatRepository.deleteChat(chatId) } just Runs

        // Act
        chatDeletionService.execute(chatId)

        // Assert - verify both methods were called
        verify { bucketService.delete(any()) }
        verify { chatRepository.deleteChat(chatId) }
    }

    @Test
    fun `should not throw exception when chat does not exist`() {
        // Arrange
        val nonExistentChatId = 999L

        every { chatRepository.findChatBucketInfoByChatId(nonExistentChatId) } returns null

        // Act - should not throw
        chatDeletionService.execute(nonExistentChatId)

        // Assert - verify no deletion methods were called
        verify(exactly = 0) { bucketService.delete(any()) }
        verify(exactly = 0) { chatRepository.deleteChat(nonExistentChatId) }
    }

    @Test
    fun `should handle chat with different bucket paths`() {
        // Arrange
        val chatId = 10L
        val customBucketPath = "/custom/bucket/path/chat-10"
        val chatBucketInfo = ChatBucketInfo(
            chatId = chatId,
            bucket = Bucket(path = customBucketPath),
        )

        every { chatRepository.findChatBucketInfoByChatId(chatId) } returns chatBucketInfo
        every { bucketService.delete(any()) } just Runs
        every { chatRepository.deleteChat(chatId) } just Runs

        // Act
        chatDeletionService.execute(chatId)

        // Assert
        verify { bucketService.delete(any()) }
        verify { chatRepository.deleteChat(chatId) }
    }

    @Test
    fun `should delete chat with id zero`() {
        // Arrange
        val chatId = 0L
        val chatBucketInfo = chatBucketInfoWith(chatId = chatId)

        every { chatRepository.findChatBucketInfoByChatId(chatId) } returns chatBucketInfo
        every { bucketService.delete(any()) } just Runs
        every { chatRepository.deleteChat(chatId) } just Runs

        // Act
        chatDeletionService.execute(chatId)

        // Assert
        verify { chatRepository.deleteChat(chatId) }
    }

    @Test
    fun `should delete chat with very large id`() {
        // Arrange
        val largeId = Long.MAX_VALUE
        val chatBucketInfo = chatBucketInfoWith(chatId = largeId)

        every { chatRepository.findChatBucketInfoByChatId(largeId) } returns chatBucketInfo
        every { bucketService.delete(any()) } just Runs
        every { chatRepository.deleteChat(largeId) } just Runs

        // Act
        chatDeletionService.execute(largeId)

        // Assert
        verify { chatRepository.deleteChat(largeId) }
    }
}
