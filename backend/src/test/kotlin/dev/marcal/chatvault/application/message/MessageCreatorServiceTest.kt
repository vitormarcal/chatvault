package dev.marcal.chatvault.application.message

import dev.marcal.chatvault.api.dto.input.NewMessageInput
import dev.marcal.chatvault.api.dto.input.NewMessagePayloadInput
import dev.marcal.chatvault.api.web.exception.ChatNotFoundException
import dev.marcal.chatvault.domain.repository.ChatRepository
import dev.marcal.chatvault.shared.chatBucketInfoWith
import dev.marcal.chatvault.shared.newMessageInputWith
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class MessageCreatorServiceTest {
    private val chatRepository: ChatRepository = mockk()
    private val messageDeduplicationService: MessageDeduplicationService = mockk()
    private val messageCreatorService = MessageCreatorService(chatRepository, messageDeduplicationService)

    @Test
    fun `should create single message successfully`() {
        // Arrange
        val chatId = 1L
        val chatBucketInfo = chatBucketInfoWith(chatId = chatId)
        val messageInput = newMessageInputWith(chatId = chatId, content = "Test message")
        val dedupedMessages = listOf(messageInput)

        every { chatRepository.findChatBucketInfoByChatId(chatId) } returns chatBucketInfo
        every { messageDeduplicationService.execute(chatId, listOf(messageInput)) } returns dedupedMessages
        every { chatRepository.saveNewMessages(any()) } returns Unit

        // Act
        messageCreatorService.execute(messageInput)

        // Assert
        verify { chatRepository.findChatBucketInfoByChatId(chatId) }
        verify { messageDeduplicationService.execute(chatId, listOf(messageInput)) }
        verify { chatRepository.saveNewMessages(any()) }
    }

    @Test
    fun `should create multiple messages successfully`() {
        // Arrange
        val chatId = 2L
        val chatBucketInfo = chatBucketInfoWith(chatId = chatId)
        val messages = listOf(
            newMessageInputWith(chatId = chatId, content = "Message 1", authorName = "User1"),
            newMessageInputWith(chatId = chatId, content = "Message 2", authorName = "User2"),
        )
        val payloadInput = NewMessagePayloadInput(chatId = chatId, messages = messages)

        every { chatRepository.findChatBucketInfoByChatId(chatId) } returns chatBucketInfo
        every { messageDeduplicationService.execute(chatId, messages) } returns messages
        every { chatRepository.saveNewMessages(any()) } returns Unit

        // Act
        messageCreatorService.execute(payloadInput)

        // Assert
        verify { chatRepository.saveNewMessages(any()) }
    }

    @Test
    fun `should throw ChatNotFoundException when chat does not exist`() {
        // Arrange
        val nonExistentChatId = 999L
        val messageInput = newMessageInputWith(chatId = nonExistentChatId)

        every { chatRepository.findChatBucketInfoByChatId(nonExistentChatId) } returns null

        // Act & Assert
        val exception = assertThrows<ChatNotFoundException> {
            messageCreatorService.execute(messageInput)
        }
        assertEquals(
            "Unable to create a message because the chat $nonExistentChatId was not found",
            exception.message,
        )
    }

    @Test
    fun `should throw IllegalStateException when message list is empty after deduplication`() {
        // Arrange
        val chatId = 3L
        val chatBucketInfo = chatBucketInfoWith(chatId = chatId)
        val messages = listOf(
            newMessageInputWith(chatId = chatId, externalId = "dup-1"),
            newMessageInputWith(chatId = chatId, externalId = "dup-1"),
        )
        val payloadInput = NewMessagePayloadInput(chatId = chatId, messages = messages)

        every { chatRepository.findChatBucketInfoByChatId(chatId) } returns chatBucketInfo
        every { messageDeduplicationService.execute(chatId, messages) } returns emptyList()

        // Act & Assert
        val exception = assertThrows<IllegalStateException> {
            messageCreatorService.execute(payloadInput)
        }
        assertEquals(
            "there are no messages to create, message list is empty for chatId=$chatId",
            exception.message,
        )
    }

    @Test
    fun `should handle mixed duplicate and unique messages`() {
        // Arrange
        val chatId = 4L
        val chatBucketInfo = chatBucketInfoWith(chatId = chatId)
        val allMessages = listOf(
            newMessageInputWith(chatId = chatId, externalId = "dup-1", content = "Dup 1"),
            newMessageInputWith(chatId = chatId, externalId = "dup-1", content = "Dup 1"),
            newMessageInputWith(chatId = chatId, externalId = "unique-1", content = "Unique 1"),
        )
        val dedupedMessages = listOf(
            newMessageInputWith(chatId = chatId, externalId = "dup-1", content = "Dup 1"),
            newMessageInputWith(chatId = chatId, externalId = "unique-1", content = "Unique 1"),
        )
        val payloadInput = NewMessagePayloadInput(chatId = chatId, messages = allMessages)

        every { chatRepository.findChatBucketInfoByChatId(chatId) } returns chatBucketInfo
        every { messageDeduplicationService.execute(chatId, allMessages) } returns dedupedMessages
        every { chatRepository.saveNewMessages(any()) } returns Unit

        // Act
        messageCreatorService.execute(payloadInput)

        // Assert
        verify { messageDeduplicationService.execute(chatId, allMessages) }
        verify { chatRepository.saveNewMessages(any()) }
    }
}
