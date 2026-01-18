package dev.marcal.chatvault.application.chat

import dev.marcal.chatvault.domain.model.Author
import dev.marcal.chatvault.domain.model.AuthorType
import dev.marcal.chatvault.domain.model.ChatLastMessage
import dev.marcal.chatvault.domain.repository.ChatRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ChatListerServiceTest {
    private val chatRepository: ChatRepository = mockk()
    private val chatListerService = ChatListerService(chatRepository)

    @Test
    fun `should return list of all chats with last message`() {
        // Arrange
        val chatLastMessages =
            sequenceOf(
                ChatLastMessage(
                    chatId = 1L,
                    chatName = "Friends Group",
                    author = Author(name = "João", type = AuthorType.USER),
                    content = "See you later!",
                    msgCreatedAt = LocalDateTime.of(2023, 8, 15, 18, 30),
                    msgCount = 45L,
                ),
                ChatLastMessage(
                    chatId = 2L,
                    chatName = "Work Chat",
                    author = Author(name = "Maria", type = AuthorType.USER),
                    content = "Project completed",
                    msgCreatedAt = LocalDateTime.of(2023, 8, 15, 17, 45),
                    msgCount = 120L,
                ),
            )

        every { chatRepository.findAllChatsWithLastMessage() } returns chatLastMessages

        // Act
        val result = chatListerService.execute()

        // Assert
        assertEquals(2, result.size)
        assertEquals(1L, result[0].chatId)
        assertEquals("Friends Group", result[0].chatName)
        assertEquals("João", result[0].authorName)
        assertEquals("See you later!", result[0].content)
        assertEquals(45L, result[0].msgCount)

        assertEquals(2L, result[1].chatId)
        assertEquals("Work Chat", result[1].chatName)
        assertEquals("Maria", result[1].authorName)
        assertEquals("Project completed", result[1].content)
        assertEquals(120L, result[1].msgCount)
    }

    @Test
    fun `should return empty list when no chats exist`() {
        // Arrange
        every { chatRepository.findAllChatsWithLastMessage() } returns emptySequence()

        // Act
        val result = chatListerService.execute()

        // Assert
        assertTrue(result.isEmpty())
        assertEquals(0, result.size)
    }

    @Test
    fun `should correctly map ChatLastMessage to ChatLastMessageOutput`() {
        // Arrange
        val testDateTime = LocalDateTime.of(2023, 12, 25, 10, 15, 30)
        val chatLastMessages =
            sequenceOf(
                ChatLastMessage(
                    chatId = 99L,
                    chatName = "Test Chat",
                    author = Author(name = "TestUser", type = AuthorType.SYSTEM),
                    content = "Test content with special chars: @#$%",
                    msgCreatedAt = testDateTime,
                    msgCount = 999L,
                ),
            )

        every { chatRepository.findAllChatsWithLastMessage() } returns chatLastMessages

        // Act
        val result = chatListerService.execute()

        // Assert
        val output = result[0]
        assertEquals(99L, output.chatId)
        assertEquals("Test Chat", output.chatName)
        assertEquals("TestUser", output.authorName)
        assertEquals("SYSTEM", output.authorType)
        assertEquals("Test content with special chars: @#$%", output.content)
        assertEquals(testDateTime, output.msgCreatedAt)
        assertEquals(999L, output.msgCount)
    }

    @Test
    fun `should handle sequence with single chat`() {
        // Arrange
        val singleChat =
            sequenceOf(
                ChatLastMessage(
                    chatId = 42L,
                    chatName = "Single Chat",
                    author = Author(name = "SingleUser", type = AuthorType.USER),
                    content = "Only message",
                    msgCreatedAt = LocalDateTime.of(2023, 1, 1, 0, 0),
                    msgCount = 1L,
                ),
            )

        every { chatRepository.findAllChatsWithLastMessage() } returns singleChat

        // Act
        val result = chatListerService.execute()

        // Assert
        assertEquals(1, result.size)
        assertEquals(42L, result[0].chatId)
    }
}
