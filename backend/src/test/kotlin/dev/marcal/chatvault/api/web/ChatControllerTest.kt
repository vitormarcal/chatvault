package dev.marcal.chatvault.api.web

import dev.marcal.chatvault.api.dto.mapper.toOutput
import dev.marcal.chatvault.application.chat.ChatAttachmentInfoService
import dev.marcal.chatvault.application.chat.ChatAttachmentService
import dev.marcal.chatvault.application.chat.ChatDeletionService
import dev.marcal.chatvault.application.chat.ChatListerService
import dev.marcal.chatvault.application.chat.ChatRenameService
import dev.marcal.chatvault.application.chat.ProfileImageService
import dev.marcal.chatvault.application.message.MessageDateFinderService
import dev.marcal.chatvault.application.message.MessageFinderService
import dev.marcal.chatvault.application.message.MessageStatisticsService
import dev.marcal.chatvault.shared.attachmentInfoOutputWith
import dev.marcal.chatvault.shared.chatLastMessageWith
import dev.marcal.chatvault.shared.messageOutputWith
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.core.io.Resource
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch

@WebMvcTest(ChatController::class)
class ChatControllerTest {
    @Autowired
    private lateinit var mvc: MockMvc

    @Autowired
    private lateinit var chatLister: ChatListerService

    @Autowired
    private lateinit var messageFinderByChatId: MessageFinderService

    @Autowired
    private lateinit var attachmentFinder: ChatAttachmentService

    @Autowired
    private lateinit var chatNameUpdater: ChatRenameService

    @Autowired
    private lateinit var attachmentInfoFinderByChatId: ChatAttachmentInfoService

    @Autowired
    private lateinit var profileImageManager: ProfileImageService

    @Autowired
    private lateinit var chatDeleter: ChatDeletionService

    @TestConfiguration
    class TestConfig {
        @Bean
        fun chatListerService() = mockk<ChatListerService>()

        @Bean
        fun messageFinderService() = mockk<MessageFinderService>()

        @Bean
        fun chatAttachmentService() = mockk<ChatAttachmentService>()

        @Bean
        fun chatRenameService() = mockk<ChatRenameService>()

        @Bean
        fun chatAttachmentInfoService() = mockk<ChatAttachmentInfoService>()

        @Bean
        fun profileImageService() = mockk<ProfileImageService>()

        @Bean
        fun chatDeletionService() = mockk<ChatDeletionService>()

        @Bean
        fun messageDateFinder() = mockk<MessageDateFinderService>()

        @Bean
        fun messageStatistics() = mockk<MessageStatisticsService>()
    }

    @Test
    fun `should list all chats with last message`() {
        // Arrange
        val chats =
            listOf(
                chatLastMessageWith(chatId = 1L, chatName = "Chat 1").toOutput(),
                chatLastMessageWith(chatId = 2L, chatName = "Chat 2").toOutput(),
            )
        every { chatLister.execute() } returns chats

        // Act & Assert
        mvc
            .get("/api/chats")
            .andExpect {
                status { isOk() }
                content { contentType("application/json") }
            }

        verify { chatLister.execute() }
    }

    @Test
    fun `should list empty chats`() {
        // Arrange
        every { chatLister.execute() } returns emptyList()

        // Act & Assert
        mvc
            .get("/api/chats")
            .andExpect {
                status { isOk() }
                jsonPath("$") { isArray() }
            }

        verify { chatLister.execute() }
    }

    @Test
    fun `should get paginated chat messages`() {
        // Arrange
        val messages =
            PageImpl(
                listOf(
                    messageOutputWith(id = 1L, content = "Message 1"),
                    messageOutputWith(id = 2L, content = "Message 2"),
                ),
                PageRequest.of(0, 10),
                2L,
            )
        every {
            messageFinderByChatId.execute(
                chatId = 1L,
                query = null,
                pageable = any(),
            )
        } returns messages

        // Act & Assert
        mvc
            .get("/api/chats/1")
            .andExpect {
                status { isOk() }
            }

        verify {
            messageFinderByChatId.execute(
                chatId = 1L,
                query = null,
                pageable = any(),
            )
        }
    }

    @Test
    fun `should search messages in chat`() {
        // Arrange
        val searchResults =
            PageImpl(
                listOf(
                    messageOutputWith(id = 1L, content = "Search result"),
                ),
                PageRequest.of(0, 10),
                1L,
            )
        every {
            messageFinderByChatId.execute(
                chatId = 1L,
                query = "search",
                pageable = any(),
            )
        } returns searchResults

        // Act & Assert
        mvc
            .get("/api/chats/1?query=search")
            .andExpect {
                status { isOk() }
            }

        verify {
            messageFinderByChatId.execute(
                chatId = 1L,
                query = "search",
                pageable = any(),
            )
        }
    }

    @Test
    fun `should delete chat and assets`() {
        // Arrange
        every { chatDeleter.execute(1L) } returns Unit

        // Act & Assert
        mvc
            .delete("/api/chats/1")
            .andExpect {
                status { isOk() }
            }

        verify { chatDeleter.execute(1L) }
    }

    @Test
    fun `should update chat name`() {
        // Arrange
        every { chatNameUpdater.execute(1L, "New Name") } returns Unit

        // Act & Assert
        mvc
            .patch("/api/chats/1/chatName/New Name")
            .andExpect {
                status { isNoContent() }
            }

        verify { chatNameUpdater.execute(1L, "New Name") }
    }

    @Test
    fun `should download message attachment`() {
        // Arrange
        val mockResource = mockk<Resource>(relaxed = true)
        every {
            attachmentFinder.execute(any())
        } returns mockResource

        // Act & Assert
        mvc
            .get("/api/chats/1/messages/100/attachment")
            .andExpect {
                status { isOk() }
            }

        verify { attachmentFinder.execute(any()) }
    }

    @Test
    fun `should list chat attachments`() {
        // Arrange
        val attachments =
            sequenceOf(
                attachmentInfoOutputWith(id = 1L, name = "file1.pdf"),
                attachmentInfoOutputWith(id = 2L, name = "file2.jpg"),
            )
        every { attachmentInfoFinderByChatId.execute(1L) } returns attachments

        // Act & Assert
        mvc
            .get("/api/chats/1/attachments")
            .andExpect {
                status { isOk() }
            }

        verify { attachmentInfoFinderByChatId.execute(1L) }
    }

    @Test
    fun `should get profile image`() {
        // Arrange
        val mockImage = mockk<Resource>(relaxed = true)
        every { profileImageManager.getImage(1L) } returns mockImage

        // Act & Assert
        mvc
            .get("/api/chats/1/profile-image")
            .andExpect {
                status { isOk() }
            }

        verify { profileImageManager.getImage(1L) }
    }
}
