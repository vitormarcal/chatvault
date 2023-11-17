package dev.marcal.chatvault.usecase

import dev.marcal.chatvault.in_out_boundary.input.NewMessageInput
import dev.marcal.chatvault.model.*
import dev.marcal.chatvault.repository.ChatRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class MessageDeduplicatorUseCaseTest {

    private val chatRepository: ChatRepository = mockk()

    private val messageDeduplicatorUseCase = MessageDeduplicatorUseCase(
        chatRepository = chatRepository
    )
    private val chatId = 1L

    @BeforeEach
    fun setup() {
        every { chatRepository.findChatBucketInfoByChatId(chatId) } returns ChatBucketInfo(
            chatId = chatId,
            bucket = Bucket("/")
        )
    }

    @Test
    fun `when has no messages stored should return input messages`() {

        val input = listOf(
            NewMessageInput(
                authorName = "Fulano",
                createdAt = LocalDateTime.of(2020, 1, 1, 5, 30),
                chatId = chatId,
                content = "Bla bla bla"
            ),
            NewMessageInput(
                authorName = "Beltrano",
                createdAt = LocalDateTime.of(2020, 1, 1, 5, 32),
                chatId = chatId,
                content = "Bla bla bla bla"
            )
        )



        every { chatRepository.findLastMessageByChatId(chatId) } returns null

        val deduplicatedMessages = messageDeduplicatorUseCase.execute(
            chatId = chatId,
            messages = input
        )

        Assertions.assertArrayEquals(input.toTypedArray(), deduplicatedMessages.toTypedArray())
    }

    @Test
    fun `when last message is equal or after stored message should return empty messages`() {

        NewMessageInput(
            authorName = "Beltrano",
            createdAt = LocalDateTime.of(2020, 1, 1, 5, 31),
            chatId = chatId,
            content = "Bla bla bla bla"
        ).apply {
            every { chatRepository.findLastMessageByChatId(chatId) } returns Message(
                author = Author(name = "Beltrano", type = AuthorType.USER),
                createdAt = LocalDateTime.of(2020, 1, 1, 5, 31),
                content = Content(text = "Bla bla bla bla"),
                externalId = null
            )

            val deduplicatedMessages = messageDeduplicatorUseCase.execute(
                chatId = chatId,
                messages = listOf(this)
            )
            Assertions.assertTrue(deduplicatedMessages.isEmpty())
        }

        NewMessageInput(
            authorName = "Beltrano",
            createdAt = LocalDateTime.of(2020, 1, 1, 5, 30),
            chatId = chatId,
            content = "Bla bla bla bla"
        ).apply {
            every { chatRepository.findLastMessageByChatId(chatId) } returns Message(
                author = Author(name = "Beltrano", type = AuthorType.USER),
                createdAt = LocalDateTime.of(2020, 1, 1, 5, 31),
                content = Content(text = "Bla bla bla bla"),
                externalId = null
            )

            val deduplicatedMessages = messageDeduplicatorUseCase.execute(
                chatId = chatId,
                messages = listOf(this)
            )
            Assertions.assertTrue(deduplicatedMessages.isEmpty())
        }

    }


    @Test
    fun `should cut off olders messages when found message equal stored message when binary searching`() {
        val now = LocalDateTime.of(2023, 1, 1, 1, 1)
        val messages = (0..10000).map {
            NewMessageInput(
                authorName = "Beltrano",
                createdAt = now.plusMinutes(it.toLong()),
                chatId = chatId,
                content = "Bla bla bla bla"
            )
        }

        val expected = messages.subList(23, messages.size).toTypedArray()

        every { chatRepository.findLastMessageByChatId(chatId) } returns Message(
            author = Author(name = "Beltrano", type = AuthorType.USER),
            createdAt = LocalDateTime.of(2023, 1, 1, 1, 23),
            content = Content(text = "Bla bla bla bla"),
            externalId = null
        )

        val deduplicatedMessages = messageDeduplicatorUseCase.execute(
            chatId = chatId,
            messages = messages
        )



        Assertions.assertArrayEquals(expected, deduplicatedMessages.toTypedArray())

    }


    @Test
    fun `should cut off olders messages when binary searching`() {
        val now = LocalDateTime.of(2023, 1, 1, 1, 1)
        val messages = (0..10000).map {
            NewMessageInput(
                authorName = "Beltrano",
                createdAt = now.plusMinutes(it.toLong()),
                chatId = chatId,
                content = "Bla bla bla bla"
            )
        }

        val expected = messages.subList(22, messages.size).toTypedArray()

        every { chatRepository.findLastMessageByChatId(chatId) } returns Message(
            author = Author(name = "Beltrano", type = AuthorType.USER),
            createdAt = LocalDateTime.of(2023, 1, 1, 1, 23),
            content = Content(text = "Bla bla bla bla bla"),
            externalId = null
        )

        val deduplicatedMessages = messageDeduplicatorUseCase.execute(
            chatId = chatId,
            messages = messages
        )

        Assertions.assertArrayEquals(expected, deduplicatedMessages.toTypedArray())

    }


}