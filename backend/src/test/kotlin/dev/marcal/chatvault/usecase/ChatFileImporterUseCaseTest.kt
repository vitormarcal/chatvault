package dev.marcal.chatvault.usecase

import dev.marcal.chatvault.domain.bucketservice.BucketService
import dev.marcal.chatvault.domain.model.Bucket
import dev.marcal.chatvault.domain.model.ChatBucketInfo
import dev.marcal.chatvault.domain.model.MessageParser
import dev.marcal.chatvault.domain.repository.ChatRepository
import dev.marcal.chatvault.ioboundary.input.FileTypeInputEnum
import dev.marcal.chatvault.ioboundary.input.NewMessagePayloadInput
import dev.marcal.chatvault.service.ChatCreator
import dev.marcal.chatvault.service.MessageCreator
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ChatFileImporterUseCaseTest {
    private val chatMessageParser = ChatMessageParserUseCase(MessageParser())
    private val chatRepository: ChatRepository = mockk()
    private val bucketService: BucketService = mockk()
    private val messageCreator: MessageCreator = mockk()
    private val chatCreator: ChatCreator = mockk()
    private val chatFileImporter =
        ChatFileImporterUseCase(
            chatMessageParser = chatMessageParser,
            chatRepository = chatRepository,
            bucketService = bucketService,
            messageCreator = messageCreator,
            chatCreator = chatCreator,
        )

    @BeforeEach
    fun setup() {
        every { chatRepository.findChatBucketInfoByChatId(1) } returns ChatBucketInfo(1, Bucket("/"))
        every { chatRepository.countChatMessages(1) } returns 1
        every { messageCreator.execute(any<NewMessagePayloadInput>()) } just Runs
        every { bucketService.save(any()) } just Runs
    }

    @Test
    fun `when receive input stream of text message file should properly execute chatParser and save messages`() {
        val inputStream =
            """
            22/09/2023 13:33 - Fulano: ‎IMG-20230922-WA0006.jpg (arquivo anexado)
            Esse é  um teste
            """.trimIndent().byteInputStream()
        chatFileImporter.execute(chatId = 1, inputStream = inputStream, fileType = FileTypeInputEnum.TEXT)

        verify { messageCreator.execute(any<NewMessagePayloadInput>()) }
        verify(exactly = 0) { bucketService.save(any()) }
    }

    @Test
    fun `when receive zip input stream should iterate properly and save files e parse text`() {
        val inputStream =
            requireNotNull(
                this.javaClass.classLoader.getResourceAsStream("test_chat.zip"),
            ) { "resource test_chat.zip not found! used by unit test" }

        chatFileImporter.execute(chatId = 1, inputStream = inputStream, fileType = FileTypeInputEnum.ZIP)

        verify(exactly = 1) { messageCreator.execute(any<NewMessagePayloadInput>()) }
        verify(exactly = 2) { bucketService.save(any()) }
    }
}
