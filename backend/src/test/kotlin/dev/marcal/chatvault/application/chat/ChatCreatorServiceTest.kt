package dev.marcal.chatvault.application.chat

import dev.marcal.chatvault.api.dto.input.NewChatInput
import dev.marcal.chatvault.domain.repository.ChatRepository
import dev.marcal.chatvault.shared.chatBucketInfoWith
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ChatCreatorServiceTest {
    private val chatRepository: ChatRepository = mockk()
    private val chatCreatorService = ChatCreatorService(chatRepository)

    @Test
    fun `should return existing chat when chat with external id already exists`() {
        // Arrange
        val externalId = "external-123"
        val existingChatInfo = chatBucketInfoWith(chatId = 1L)
        val input = NewChatInput(name = "Test Chat", externalId = externalId)

        every { chatRepository.existsByExternalId(externalId) } returns true
        every { chatRepository.findChatBucketInfoByExternalId(externalId) } returns existingChatInfo

        // Act
        val result = chatCreatorService.createIfNotExists(input)

        // Assert
        assertEquals(existingChatInfo, result)
        assertEquals(1L, result.chatId)
    }

    @Test
    fun `should create new chat when external id does not exist`() {
        // Arrange
        val externalId = "external-456"
        val chatName = "New Chat"
        val newChatInfo = chatBucketInfoWith(chatId = 2L)
        val input = NewChatInput(name = chatName, externalId = externalId)

        every { chatRepository.existsByExternalId(externalId) } returns false
        every { chatRepository.create(any()) } returns newChatInfo

        // Act
        val result = chatCreatorService.createIfNotExists(input)

        // Assert
        assertEquals(newChatInfo, result)
        assertEquals(2L, result.chatId)
        verify { chatRepository.create(any()) }
    }

    @Test
    fun `should handle null external id and create new chat`() {
        // Arrange
        val chatName = "Chat without external id"
        val newChatInfo = chatBucketInfoWith(chatId = 3L)
        val input = NewChatInput(name = chatName, externalId = null)

        every { chatRepository.create(any()) } returns newChatInfo

        // Act
        val result = chatCreatorService.createIfNotExists(input)

        // Assert
        assertEquals(newChatInfo, result)
        verify { chatRepository.create(any()) }
    }

    @Test
    fun `should find existing chat by name`() {
        // Arrange
        val chatName = "Existing Group"
        val existingChat = chatBucketInfoWith(chatId = 4L)

        every { chatRepository.findChatBucketInfoByChatName(chatName) } returns existingChat

        // Act
        val result = chatCreatorService.findOrCreateByName(chatName)

        // Assert
        assertEquals(existingChat, result)
        assertEquals(4L, result.chatId)
    }

    @Test
    fun `should create new chat when not found by name`() {
        // Arrange
        val chatName = "New Group"
        val newChatInfo = chatBucketInfoWith(chatId = 5L)

        every { chatRepository.findChatBucketInfoByChatName(chatName) } returns null
        every { chatRepository.existsByExternalId(any()) } returns false
        every { chatRepository.create(any()) } returns newChatInfo

        // Act
        val result = chatCreatorService.findOrCreateByName(chatName)

        // Assert
        assertEquals(newChatInfo, result)
        assertEquals(5L, result.chatId)
    }

    @Test
    fun `should create chat with unique bucket paths`() {
        // Arrange
        val input1 = NewChatInput(name = "Chat 1", externalId = "ext-1")
        val input2 = NewChatInput(name = "Chat 2", externalId = "ext-2")
        val newChatInfo1 = chatBucketInfoWith(chatId = 6L)
        val newChatInfo2 = chatBucketInfoWith(chatId = 7L)

        every { chatRepository.existsByExternalId("ext-1") } returns false
        every { chatRepository.existsByExternalId("ext-2") } returns false
        every { chatRepository.create(any()) } returnsMany listOf(newChatInfo1, newChatInfo2)

        // Act
        val result1 = chatCreatorService.createIfNotExists(input1)
        val result2 = chatCreatorService.createIfNotExists(input2)

        // Assert
        assertEquals(6L, result1.chatId)
        assertEquals(7L, result2.chatId)
    }
}
