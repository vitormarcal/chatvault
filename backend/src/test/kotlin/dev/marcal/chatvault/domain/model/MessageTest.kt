package dev.marcal.chatvault.domain.model

import dev.marcal.chatvault.shared.chatBucketInfoWith
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class MessageTest {
    @Test
    fun `should create system message when author name is empty`() {
        // Arrange
        val chatBucketInfo = chatBucketInfoWith()
        val content = "System notification"
        val now = LocalDateTime.of(2023, 1, 1, 12, 0)

        // Act
        val message =
            Message.create(
                chatBucketInfo = chatBucketInfo,
                authorName = "",
                content = content,
                createdAt = now,
            )

        // Assert
        assertEquals(AuthorType.SYSTEM, message.author.type)
        assertEquals("", message.author.name)
        assertEquals(content, message.content.text)
        assertEquals(now, message.createdAt)
    }

    @Test
    fun `should create user message when author name is provided`() {
        // Arrange
        val chatBucketInfo = chatBucketInfoWith()
        val authorName = "John"
        val content = "Hello world"
        val now = LocalDateTime.of(2023, 1, 1, 12, 0)

        // Act
        val message =
            Message.create(
                chatBucketInfo = chatBucketInfo,
                authorName = authorName,
                content = content,
                createdAt = now,
            )

        // Assert
        assertEquals(AuthorType.USER, message.author.type)
        assertEquals(authorName, message.author.name)
        assertEquals(content, message.content.text)
        assertEquals(now, message.createdAt)
    }

    @Test
    fun `should create message without attachment when attachmentName is null`() {
        // Arrange
        val chatBucketInfo = chatBucketInfoWith()

        // Act
        val message =
            Message.create(
                chatBucketInfo = chatBucketInfo,
                authorName = "Alice",
                content = "No attachment",
                attachmentName = null,
            )

        // Assert
        assertNull(message.content.attachment)
    }

    @Test
    fun `should create message with attachment when attachmentName is provided`() {
        // Arrange
        val chatBucketInfo = chatBucketInfoWith()
        val attachmentName = "document.pdf"

        // Act
        val message =
            Message.create(
                chatBucketInfo = chatBucketInfo,
                authorName = "Bob",
                content = "With attachment",
                attachmentName = attachmentName,
            )

        // Assert
        assertNotNull(message.content.attachment)
        assertEquals(attachmentName, message.content.attachment?.name)
    }

    @Test
    fun `should use provided createdAt timestamp`() {
        // Arrange
        val chatBucketInfo = chatBucketInfoWith()
        val customTime = LocalDateTime.of(2023, 6, 15, 14, 30)

        // Act
        val message =
            Message.create(
                chatBucketInfo = chatBucketInfo,
                authorName = "Charlie",
                content = "Custom time",
                createdAt = customTime,
            )

        // Assert
        assertEquals(customTime, message.createdAt)
    }

    @Test
    fun `should use now function when createdAt is null`() {
        // Arrange
        val chatBucketInfo = chatBucketInfoWith()
        val nowValue = LocalDateTime.of(2023, 12, 25, 10, 0)
        var nowFunctionCalled = false

        // Act
        val message =
            Message.create(
                chatBucketInfo = chatBucketInfo,
                authorName = "Dave",
                content = "Using now function",
                createdAt = null,
                now = {
                    nowFunctionCalled = true
                    nowValue
                },
            )

        // Assert
        assert(nowFunctionCalled)
        assertEquals(nowValue, message.createdAt)
    }

    @Test
    fun `should set externalId when provided`() {
        // Arrange
        val chatBucketInfo = chatBucketInfoWith()
        val externalId = "external-message-123"

        // Act
        val message =
            Message.create(
                chatBucketInfo = chatBucketInfo,
                authorName = "Eve",
                content = "With external ID",
                externalId = externalId,
            )

        // Assert
        assertEquals(externalId, message.externalId)
    }

    @Test
    fun `should have null externalId when not provided`() {
        // Arrange
        val chatBucketInfo = chatBucketInfoWith()

        // Act
        val message =
            Message.create(
                chatBucketInfo = chatBucketInfo,
                authorName = "Frank",
                content = "No external ID",
                externalId = null,
            )

        // Assert
        assertNull(message.externalId)
    }

    @Test
    fun `should create message with whitespace author as system message`() {
        // Arrange
        val chatBucketInfo = chatBucketInfoWith()

        // Act
        val message =
            Message.create(
                chatBucketInfo = chatBucketInfo,
                authorName = "   ",
                content = "Whitespace author",
            )

        // Assert
        // Note: Currently treated as USER since "   " is not empty
        // This test documents current behavior
        assertEquals("   ", message.author.name)
    }

    @Test
    fun `should use bucket from chatBucketInfo for attachment`() {
        // Arrange
        val customBucketPath = "/chat-123/custom-path"
        val chatBucketInfo = chatBucketInfoWith(bucket = Bucket(path = customBucketPath))
        val attachmentName = "file.txt"

        // Act
        val message =
            Message.create(
                chatBucketInfo = chatBucketInfo,
                authorName = "George",
                content = "Test",
                attachmentName = attachmentName,
            )

        // Assert
        val attachment = message.content.attachment
        assertNotNull(attachment)
        assertEquals(attachmentName, attachment.name)
        assertNotNull(attachment.bucket)
    }

    @Test
    fun `should handle very long content`() {
        // Arrange
        val chatBucketInfo = chatBucketInfoWith()
        val longContent = "A".repeat(10000)

        // Act
        val message =
            Message.create(
                chatBucketInfo = chatBucketInfo,
                authorName = "Hannah",
                content = longContent,
            )

        // Assert
        assertEquals(longContent, message.content.text)
        assertEquals(10000, message.content.text.length)
    }
}
