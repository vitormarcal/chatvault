package dev.marcal.chatvault.api.dto.mapper

import dev.marcal.chatvault.shared.attachmentSummaryWith
import dev.marcal.chatvault.shared.attachmentWith
import dev.marcal.chatvault.shared.authorWith
import dev.marcal.chatvault.shared.chatLastMessageWith
import dev.marcal.chatvault.shared.messageOutputWith
import dev.marcal.chatvault.shared.messageWith
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class MapperTest {
    @Test
    fun `should map ChatLastMessage to ChatLastMessageOutput`() {
        // Arrange
        val chatLastMessage = chatLastMessageWith(
            chatId = 1L,
            chatName = "Test Chat",
            author = authorWith(name = "John"),
            msgCount = 10,
        )

        // Act
        val result = chatLastMessage.toOutput()

        // Assert
        assertEquals(1L, result.chatId)
        assertEquals("Test Chat", result.chatName)
        assertEquals("John", result.authorName)
        assertEquals(10, result.msgCount)
    }

    @Test
    fun `should map Message to MessageOutput`() {
        // Arrange
        val message = messageWith(
            id = 1L,
            author = "Alice",
            content = "Hello",
        )

        // Act
        val result = message.toOutput()

        // Assert
        assertEquals(1L, result.id)
        assertEquals("Alice", result.author)
        assertEquals("Hello", result.content)
        assertNotNull(result.createdAt)
    }

    @Test
    fun `should map Message with attachment to MessageOutput`() {
        // Arrange
        val message = messageWith(
            id = 2L,
            author = "Bob",
            content = "Message with file",
            attachment = attachmentWith(name = "document.pdf"),
        )

        // Act
        val result = message.toOutput()

        // Assert
        assertEquals("document.pdf", result.attachmentName)
    }

    @Test
    fun `should map MessageOutput to NewMessageInput`() {
        // Arrange
        val messageOutput = messageOutputWith(
            author = "Charlie",
            content = "Test message",
        )
        val chatId = 5L

        // Act
        val result = messageOutput.toNewMessageInput(chatId)

        // Assert
        assertEquals(chatId, result.chatId)
        assertEquals("Charlie", result.authorName)
        assertEquals("Test message", result.content)
    }

    @Test
    fun `should map MessageOutput with attachment to NewMessageInput`() {
        // Arrange
        val messageOutput = messageOutputWith(
            author = "Dave",
            content = "Message",
            attachmentName = "image.jpg",
        )

        // Act
        val result = messageOutput.toNewMessageInput(10L)

        // Assert
        assertNotNull(result.attachment)
        assertEquals("image.jpg", result.attachment?.name)
    }

    @Test
    fun `should map AttachmentSummary to AttachmentInfoOutput`() {
        // Arrange
        val attachment = attachmentSummaryWith(
            id = 1L,
            name = "photo.png",
        )

        // Act
        val result = attachment.toOutput()

        // Assert
        assertEquals(1L, result.id)
        assertEquals("photo.png", result.name)
    }

    @Test
    fun `should handle null author in MessageOutput mapping`() {
        // Arrange
        val messageOutput = messageOutputWith(
            author = null,
            content = "Anonymous message",
        )

        // Act
        val result = messageOutput.toNewMessageInput(3L)

        // Assert
        assertEquals("", result.authorName)
        assertEquals("Anonymous message", result.content)
    }

    @Test
    fun `should handle null attachment in MessageOutput mapping`() {
        // Arrange
        val messageOutput = messageOutputWith(
            author = "Eve",
            content = "No attachment",
            attachmentName = null,
        )

        // Act
        val result = messageOutput.toNewMessageInput(7L)

        // Assert
        assertNull(result.attachment)
    }
}
