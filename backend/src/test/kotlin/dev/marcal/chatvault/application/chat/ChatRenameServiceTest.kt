package dev.marcal.chatvault.application.chat

import dev.marcal.chatvault.domain.repository.ChatRepository
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class ChatRenameServiceTest {
    private val chatRepository: ChatRepository = mockk()
    private val chatRenameService = ChatRenameService(chatRepository)

    @Test
    fun `should successfully rename chat when provided valid chatId and name`() {
        // Arrange
        val chatId = 1L
        val newName = "Updated Chat Name"

        every { chatRepository.existsByChatId(chatId) } returns true
        every { chatRepository.setChatNameByChatId(chatId, newName) } just Runs

        // Act
        chatRenameService.execute(chatId, newName)

        // Assert
        verify { chatRepository.existsByChatId(chatId) }
        verify { chatRepository.setChatNameByChatId(chatId, newName) }
    }

    @Test
    fun `should throw exception when chat name is blank`() {
        // Arrange
        val chatId = 1L
        val blankName = "   "

        // Act & Assert
        val exception =
            assertThrows<IllegalArgumentException> {
                chatRenameService.execute(chatId, blankName)
            }
        assertEquals("chatName must not be empty", exception.message)
    }

    @Test
    fun `should throw exception when chat name is empty string`() {
        // Arrange
        val chatId = 1L
        val emptyName = ""

        // Act & Assert
        val exception =
            assertThrows<IllegalArgumentException> {
                chatRenameService.execute(chatId, emptyName)
            }
        assertEquals("chatName must not be empty", exception.message)
    }

    @Test
    fun `should throw exception when chat does not exist`() {
        // Arrange
        val nonExistentChatId = 999L
        val newName = "New Name"

        every { chatRepository.existsByChatId(nonExistentChatId) } returns false

        // Act & Assert
        val exception =
            assertThrows<IllegalArgumentException> {
                chatRenameService.execute(nonExistentChatId, newName)
            }
        assertEquals("chat not found", exception.message)
    }

    @Test
    fun `should handle special characters in chat name`() {
        // Arrange
        val chatId = 5L
        val specialName = "Chat @#$% & Friends! 🎉"

        every { chatRepository.existsByChatId(chatId) } returns true
        every { chatRepository.setChatNameByChatId(chatId, specialName) } just Runs

        // Act
        chatRenameService.execute(chatId, specialName)

        // Assert
        verify { chatRepository.setChatNameByChatId(chatId, specialName) }
    }

    @Test
    fun `should handle very long chat names`() {
        // Arrange
        val chatId = 10L
        val longName = "A".repeat(500)

        every { chatRepository.existsByChatId(chatId) } returns true
        every { chatRepository.setChatNameByChatId(chatId, longName) } just Runs

        // Act
        chatRenameService.execute(chatId, longName)

        // Assert
        verify { chatRepository.setChatNameByChatId(chatId, longName) }
    }
}
