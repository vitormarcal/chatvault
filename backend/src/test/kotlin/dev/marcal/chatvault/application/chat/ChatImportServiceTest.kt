package dev.marcal.chatvault.application.chat

import dev.marcal.chatvault.api.dto.input.FileTypeInputEnum
import dev.marcal.chatvault.api.dto.input.NewMessagePayloadInput
import dev.marcal.chatvault.application.message.ChatMessageParserService
import dev.marcal.chatvault.application.message.MessageCreatorService
import dev.marcal.chatvault.domain.bucket.BucketService
import dev.marcal.chatvault.domain.model.Bucket
import dev.marcal.chatvault.domain.model.ChatBucketInfo
import dev.marcal.chatvault.domain.model.MessageParser
import dev.marcal.chatvault.domain.repository.ChatRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ChatImportServiceTest {
    private val chatMessageParser = ChatMessageParserService(MessageParser())
    private val chatRepository: ChatRepository = mockk()
    private val bucketService: BucketService = mockk()
    private val messageCreator: MessageCreatorService = mockk()
    private val chatCreator: ChatCreatorService = mockk()
    private val chatImportService =
        ChatImportService(
            chatMessageParser = chatMessageParser,
            chatRepository = chatRepository,
            bucketService = bucketService,
            messageCreator = messageCreator,
            chatCreator = chatCreator,
        )

    @BeforeEach
    fun setup() {
        every { chatRepository.findChatBucketInfoByChatId(any()) } returns ChatBucketInfo(1, Bucket("/"))
        every { chatRepository.countChatMessages(any()) } returns 1
        every { messageCreator.execute(any<NewMessagePayloadInput>()) } returns Unit
        every { bucketService.save(any()) } returns Unit
    }

    @Test
    fun `should parse text message file and create messages`() {
        // Arrange
        val inputStream =
            """
            22/09/2023 13:33 - Fulano: Test message
            23/09/2023 14:45 - Beltrana: Response message
            """.trimIndent().byteInputStream()

        // Act
        chatImportService.execute(chatId = 1, inputStream = inputStream, fileType = FileTypeInputEnum.TEXT)

        // Assert
        verify { messageCreator.execute(any<NewMessagePayloadInput>()) }
        verify(exactly = 0) { bucketService.save(any()) }
    }

    @Test
    fun `should import zip file with text and attachments`() {
        // Arrange
        val inputStream =
            requireNotNull(
                this.javaClass.classLoader.getResourceAsStream("test_chat.zip"),
            ) { "resource test_chat.zip not found! used by unit test" }

        // Act
        chatImportService.execute(chatId = 1, inputStream = inputStream, fileType = FileTypeInputEnum.ZIP)

        // Assert
        verify(exactly = 1) { messageCreator.execute(any<NewMessagePayloadInput>()) }
        verify(exactly = 2) { bucketService.save(any()) }
    }

    @Test
    fun `should handle empty text file`() {
        // Arrange
        val inputStream = "".byteInputStream()

        // Act
        chatImportService.execute(chatId = 2, inputStream = inputStream, fileType = FileTypeInputEnum.TEXT)

        // Assert
        verify { chatRepository.countChatMessages(2) }
    }

    @Test
    fun `should count existing messages before import`() {
        // Arrange
        val chatId = 3L
        val inputStream = "25/09/2023 10:00 - User: Message".byteInputStream()

        every { chatRepository.countChatMessages(chatId) } returns 5

        // Act
        chatImportService.execute(chatId = chatId, inputStream = inputStream, fileType = FileTypeInputEnum.TEXT)

        // Assert
        verify { chatRepository.countChatMessages(chatId) }
    }

    @Test
    fun `should handle messages with special characters`() {
        // Arrange
        val chatId = 4L
        val inputStream =
            """
            26/09/2023 11:30 - User@Name: Message with @#$% chars!
            """.trimIndent().byteInputStream()

        // Act
        chatImportService.execute(chatId = chatId, inputStream = inputStream, fileType = FileTypeInputEnum.TEXT)

        // Assert
        verify { messageCreator.execute(any<NewMessagePayloadInput>()) }
    }

    @Test
    fun `should process multiple files in zip correctly`() {
        // Arrange
        val chatId = 5L
        val zipResource =
            requireNotNull(
                this.javaClass.classLoader.getResourceAsStream("test_chat.zip"),
            ) { "test_chat.zip resource not found" }

        // Act
        chatImportService.execute(chatId = chatId, inputStream = zipResource, fileType = FileTypeInputEnum.ZIP)

        // Assert
        verify { messageCreator.execute(any<NewMessagePayloadInput>()) }
        verify { bucketService.save(any()) }
    }
}
