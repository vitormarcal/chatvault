package dev.marcal.chatvault.api.web

import dev.marcal.chatvault.api.dto.input.NewMessageInput
import dev.marcal.chatvault.application.message.MessageCreatorService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

@WebMvcTest(MessageController::class)
class MessageControllerTest {
    @Autowired
    private lateinit var mvc: MockMvc

    @Autowired
    private lateinit var messageCreator: MessageCreatorService

    @TestConfiguration
    class TestConfig {
        @Bean
        fun messageCreatorService() = mockk<MessageCreatorService>()
    }

    @Test
    fun `should create new message successfully`() {
        // Arrange
        every { messageCreator.execute(input = any<NewMessageInput>()) } returns Unit

        // Act & Assert
        mvc
            .post("/api/messages") {
                contentType = MediaType.APPLICATION_JSON
                content =
                    """
                    {
                        "chatId": 1,
                        "authorName": "John",
                        "content": "Test message",
                        "createdAt": "2023-01-01T10:00:00"
                    }
                    """.trimIndent()
            }.andExpect {
                status { isNoContent() }
            }

        verify { messageCreator.execute(input = any<NewMessageInput>()) }
    }

    @Test
    fun `should create message with attachment`() {
        // Arrange
        every { messageCreator.execute(input = any<NewMessageInput>()) } returns Unit

        // Act & Assert
        mvc
            .post("/api/messages") {
                contentType = MediaType.APPLICATION_JSON
                content =
                    """
                    {
                        "chatId": 2,
                        "authorName": "Alice",
                        "content": "Message with file",
                        "attachment": {
                            "name": "document.pdf",
                            "content": ""
                        }
                    }
                    """.trimIndent()
            }.andExpect {
                status { isNoContent() }
            }

        verify { messageCreator.execute(input = any<NewMessageInput>()) }
    }

    @Test
    fun `should create message without attachment`() {
        // Arrange
        every { messageCreator.execute(input = any<NewMessageInput>()) } returns Unit

        // Act & Assert
        mvc
            .post("/api/messages") {
                contentType = MediaType.APPLICATION_JSON
                content =
                    """
                    {
                        "chatId": 3,
                        "authorName": "Bob",
                        "content": "Simple message"
                    }
                    """.trimIndent()
            }.andExpect {
                status { isNoContent() }
            }

        verify { messageCreator.execute(input = any<NewMessageInput>()) }
    }
}
