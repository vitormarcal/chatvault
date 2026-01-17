package dev.marcal.chatvault.api.web

import dev.marcal.chatvault.application.chat.BucketDiskImportService
import dev.marcal.chatvault.application.chat.ChatExportService
import dev.marcal.chatvault.application.chat.ChatImportService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.multipart
import org.springframework.test.web.servlet.post
import org.springframework.web.server.ResponseStatusException

@WebMvcTest(ChatImportExportController::class)
class ChatImportExportControllerTest {
    @Autowired
    private lateinit var mvc: MockMvc

    @Autowired
    private lateinit var chatFileImporter: ChatImportService

    @Autowired
    private lateinit var bucketDiskImporter: BucketDiskImportService

    @Autowired
    private lateinit var chatFileExporter: ChatExportService

    @TestConfiguration
    class TestConfig {
        @Bean
        fun chatImportService() = mockk<ChatImportService>()

        @Bean
        fun bucketDiskImportService() = mockk<BucketDiskImportService>()

        @Bean
        fun chatExportService() = mockk<ChatExportService>()
    }

    @Test
    fun `should execute disk import`() {
        // Arrange
        every { bucketDiskImporter.execute() } returns Unit

        // Act & Assert
        mvc.post("/api/chats/disk-import")
            .andExpect {
                status { isOk() }
            }

        verify { bucketDiskImporter.execute() }
    }

    @Test
    fun `should import text file to existing chat`() {
        // Arrange
        val file = MockMultipartFile(
            "file",
            "chat.txt",
            MediaType.TEXT_PLAIN_VALUE,
            "22/09/2023 13:33 - User: Test message".toByteArray(),
        )
        every { chatFileImporter.execute(chatId = 1L, inputStream = any(), fileType = any()) } returns Unit

        // Act & Assert
        mvc.multipart("/api/chats/1/messages/import") {
            file(file)
        }
            .andExpect {
                status { isNoContent() }
            }

        verify { chatFileImporter.execute(chatId = 1L, inputStream = any(), fileType = any()) }
    }

    @Test
    fun `should import zip file to existing chat`() {
        // Arrange
        val zipFile = MockMultipartFile(
            "file",
            "chat.zip",
            "application/zip",
            "PK\u0003\u0004".toByteArray(),
        )
        every { chatFileImporter.execute(chatId = 2L, inputStream = any(), fileType = any()) } returns Unit

        // Act & Assert
        mvc.multipart("/api/chats/2/messages/import") {
            file(zipFile)
        }
            .andExpect {
                status { isNoContent() }
            }

        verify { chatFileImporter.execute(chatId = 2L, inputStream = any(), fileType = any()) }
    }

    @Test
    fun `should create new chat and import file`() {
        // Arrange
        val file = MockMultipartFile(
            "file",
            "chat.txt",
            MediaType.TEXT_PLAIN_VALUE,
            "23/09/2023 14:45 - Bot: Welcome".toByteArray(),
        )
        every { chatFileImporter.execute(chatName = "New Chat", inputStream = any(), fileType = any()) } returns Unit

        // Act & Assert
        mvc.multipart("/api/chats/import/New Chat") {
            file(file)
        }
            .andExpect {
                status { isNoContent() }
            }

        verify { chatFileImporter.execute(chatName = "New Chat", inputStream = any(), fileType = any()) }
    }

    @Test
    fun `should reject empty file for import`() {
        // Arrange
        val emptyFile = MockMultipartFile(
            "file",
            "empty.txt",
            MediaType.TEXT_PLAIN_VALUE,
            ByteArray(0),
        )

        // Act & Assert
        mvc.multipart("/api/chats/1/messages/import") {
            file(emptyFile)
        }
            .andExpect {
                status { isBadRequest() }
            }
    }

    @Test
    fun `should reject file without content type`() {
        // Arrange
        val file = MockMultipartFile("file", "chat.txt", null, "content".toByteArray())

        // Act & Assert
        mvc.multipart("/api/chats/1/messages/import") {
            file(file)
        }
            .andExpect {
                status { isBadRequest() }
            }
    }

    @Test
    fun `should reject unsupported file type`() {
        // Arrange
        val file = MockMultipartFile(
            "file",
            "image.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            "image-content".toByteArray(),
        )

        // Act & Assert
        mvc.multipart("/api/chats/1/messages/import") {
            file(file)
        }
            .andExpect {
                status { isUnsupportedMediaType() }
            }
    }

    @Test
    fun `should export single chat`() {
        // Arrange
        val mockResource = ByteArrayResource("zip-content".toByteArray())
        every { chatFileExporter.execute(1L) } returns mockResource

        // Act & Assert
        mvc.get("/api/chats/1/export")
            .andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_OCTET_STREAM) }
            }

        verify { chatFileExporter.execute(1L) }
    }

    @Test
    fun `should export all chats`() {
        // Arrange
        val mockResource = ByteArrayResource("all-chats-zip".toByteArray())
        every { chatFileExporter.executeAll() } returns mockResource

        // Act & Assert
        mvc.get("/api/chats/export/all")
            .andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_OCTET_STREAM) }
            }

        verify { chatFileExporter.executeAll() }
    }

    @Test
    fun `should accept application-x-zip-compressed content type`() {
        // Arrange
        val zipFile = MockMultipartFile(
            "file",
            "chat.zip",
            "application/x-zip-compressed",
            "PK\u0003\u0004".toByteArray(),
        )
        every { chatFileImporter.execute(chatId = 3L, inputStream = any(), fileType = any()) } returns Unit

        // Act & Assert
        mvc.multipart("/api/chats/3/messages/import") {
            file(zipFile)
        }
            .andExpect {
                status { isNoContent() }
            }
    }

    @Test
    fun `should accept octet-stream as zip content type`() {
        // Arrange
        val zipFile = MockMultipartFile(
            "file",
            "archive.zip",
            "application/octet-stream",
            "PK\u0003\u0004".toByteArray(),
        )
        every { chatFileImporter.execute(chatId = 4L, inputStream = any(), fileType = any()) } returns Unit

        // Act & Assert
        mvc.multipart("/api/chats/4/messages/import") {
            file(zipFile)
        }
            .andExpect {
                status { isNoContent() }
            }
    }
}
