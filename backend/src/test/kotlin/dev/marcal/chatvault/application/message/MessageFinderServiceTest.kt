package dev.marcal.chatvault.application.message

import dev.marcal.chatvault.domain.model.Author
import dev.marcal.chatvault.domain.model.AuthorType
import dev.marcal.chatvault.domain.model.Content
import dev.marcal.chatvault.domain.model.Message
import dev.marcal.chatvault.domain.repository.ChatRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MessageFinderServiceTest {
    private val chatRepository: ChatRepository = mockk()
    private val messageFinderService = MessageFinderService(chatRepository)

    @Test
    fun `should find messages with query and pagination`() {
        // Arrange
        val chatId = 1L
        val query = "test"
        val pageable = PageRequest.of(0, 10)
        val messages = listOf(
            Message(
                id = 1L,
                author = Author(name = "John", type = AuthorType.USER),
                content = Content(text = "This is a test message"),
                createdAt = LocalDateTime.of(2023, 1, 1, 12, 0),
                externalId = "ext-1",
            ),
            Message(
                id = 2L,
                author = Author(name = "Jane", type = AuthorType.USER),
                content = Content(text = "Another test"),
                createdAt = LocalDateTime.of(2023, 1, 1, 13, 0),
                externalId = "ext-2",
            ),
        )
        val page = PageImpl(messages, pageable, 2L)

        every { chatRepository.findMessagesBy(chatId, query, pageable) } returns page

        // Act
        val result = messageFinderService.execute(chatId, query, pageable)

        // Assert
        assertEquals(2, result.content.size)
        assertEquals(2L, result.totalElements)
        assertEquals("John", result.content[0].author)
        assertEquals("Jane", result.content[1].author)
        verify { chatRepository.findMessagesBy(chatId, query, pageable) }
    }

    @Test
    fun `should find all messages without query`() {
        // Arrange
        val chatId = 5L
        val pageable = PageRequest.of(0, 20)
        val messages = listOf(
            Message(
                id = 1L,
                author = Author(name = "Alice", type = AuthorType.USER),
                content = Content(text = "Message 1"),
                createdAt = LocalDateTime.of(2023, 2, 1, 10, 0),
                externalId = "ext-1",
            ),
        )
        val page = PageImpl(messages, pageable, 1L)

        every { chatRepository.findMessagesBy(chatId, null, pageable) } returns page

        // Act
        val result = messageFinderService.execute(chatId, null, pageable)

        // Assert
        assertEquals(1, result.content.size)
        assertEquals("Message 1", result.content[0].content)
        verify { chatRepository.findMessagesBy(chatId, null, pageable) }
    }

    @Test
    fun `should return empty page when no messages match query`() {
        // Arrange
        val chatId = 1L
        val query = "nonexistent"
        val pageable = PageRequest.of(0, 10)
        val emptyPage = PageImpl<Message>(emptyList(), pageable, 0L)

        every { chatRepository.findMessagesBy(chatId, query, pageable) } returns emptyPage

        // Act
        val result = messageFinderService.execute(chatId, query, pageable)

        // Assert
        assertTrue(result.content.isEmpty())
        assertEquals(0L, result.totalElements)
    }

    @Test
    fun `should handle pagination correctly`() {
        // Arrange
        val chatId = 1L
        val pageable = PageRequest.of(1, 5) // Page 1, size 5
        val messages = (11..15).map {
            Message(
                id = it.toLong(),
                author = Author(name = "User", type = AuthorType.USER),
                content = Content(text = "Message $it"),
                createdAt = LocalDateTime.of(2023, 1, it, 12, 0),
                externalId = "ext-$it",
            )
        }
        val page = PageImpl(messages, pageable, 100L) // Total 100 messages

        every { chatRepository.findMessagesBy(chatId, null, pageable) } returns page

        // Act
        val result = messageFinderService.execute(chatId, null, pageable)

        // Assert
        assertEquals(5, result.content.size)
        assertEquals(100L, result.totalElements)
        assertEquals(20, result.totalPages)
        assertEquals(1, result.number)
    }

    @Test
    fun `should return all messages as sequence without pagination`() {
        // Arrange
        val chatId = 1L
        val messages = (1..50).map {
            Message(
                id = it.toLong(),
                author = Author(name = "User", type = AuthorType.USER),
                content = Content(text = "Message $it"),
                createdAt = LocalDateTime.of(2023, 1, 1, (it % 24), 0),
                externalId = "ext-$it",
            )
        }

        // Mock to return messages on first page, empty on next pages (to stop iteration)
        every { chatRepository.findMessagesBy(chatId, null, any()) } answers {
            val pageable = args[2] as org.springframework.data.domain.Pageable
            when (pageable.pageNumber) {
                0 -> PageImpl(messages.take(50), pageable, 50L)
                else -> PageImpl(emptyList(), pageable, 50L)
            }
        }

        // Act
        val result = messageFinderService.execute(chatId).toList()

        // Assert
        assertEquals(50, result.size)
        assertEquals("Message 1", result[0].content)
        assertEquals("Message 50", result[49].content)
    }

    @Test
    fun `should map message to output correctly with all fields`() {
        // Arrange
        val chatId = 1L
        val pageable = PageRequest.of(0, 10)
        val messages = listOf(
            Message(
                id = 42L,
                author = Author(name = "TestUser", type = AuthorType.SYSTEM),
                content = Content(text = "System message", attachment = null),
                createdAt = LocalDateTime.of(2023, 12, 25, 15, 30, 45),
                externalId = "external-42",
            ),
        )
        val page = PageImpl(messages, pageable, 1L)

        every { chatRepository.findMessagesBy(chatId, null, pageable) } returns page

        // Act
        val result = messageFinderService.execute(chatId, null, pageable)

        // Assert
        val output = result.content[0]
        assertEquals(42L, output.id)
        assertEquals("TestUser", output.author)
        assertEquals("SYSTEM", output.authorType)
        assertEquals("System message", output.content)
        assertEquals(null, output.attachmentName)
    }
}
