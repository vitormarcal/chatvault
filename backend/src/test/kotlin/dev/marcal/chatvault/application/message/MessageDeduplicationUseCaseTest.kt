package dev.marcal.chatvault.application.message

import dev.marcal.chatvault.api.dto.input.NewMessageInput
import dev.marcal.chatvault.domain.model.Author
import dev.marcal.chatvault.domain.model.AuthorType
import dev.marcal.chatvault.domain.model.Bucket
import dev.marcal.chatvault.domain.model.ChatBucketInfo
import dev.marcal.chatvault.domain.model.Content
import dev.marcal.chatvault.domain.model.Message
import dev.marcal.chatvault.domain.repository.ChatRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class MessageDeduplicationUseCaseTest {
    private val chatRepository: ChatRepository = mockk()

    private val messageDeduplicationService =
        MessageDeduplicationService(
            chatRepository = chatRepository,
        )
    private val chatId = 1L

    @BeforeEach
    fun setup() {
        every { chatRepository.findChatBucketInfoByChatId(chatId) } returns
            ChatBucketInfo(
                chatId = chatId,
                bucket = Bucket("/"),
            )
    }

    @Test
    fun `when has no messages stored should return input messages`() {
        val input =
            listOf(
                NewMessageInput(
                    authorName = "Fulano",
                    createdAt = LocalDateTime.of(2020, 1, 1, 5, 30),
                    chatId = chatId,
                    content = "Bla bla bla",
                ),
                NewMessageInput(
                    authorName = "Beltrano",
                    createdAt = LocalDateTime.of(2020, 1, 1, 5, 32),
                    chatId = chatId,
                    content = "Bla bla bla bla",
                ),
            )

        every { chatRepository.findLastMessageByChatId(chatId) } returns null

        val deduplicatedMessages =
            messageDeduplicationService.execute(
                chatId = chatId,
                messages = input,
            )

        Assertions.assertArrayEquals(input.toTypedArray(), deduplicatedMessages.toTypedArray())
    }

    @Test
    fun `when last message is equal or after stored message should return empty messages`() {
        NewMessageInput(
            authorName = "Beltrano",
            createdAt = LocalDateTime.of(2020, 1, 1, 5, 31),
            chatId = chatId,
            content = "Bla bla bla bla",
        ).apply {
            every { chatRepository.findLastMessageByChatId(chatId) } returns
                Message(
                    author = Author(name = "Beltrano", type = AuthorType.USER),
                    createdAt = LocalDateTime.of(2020, 1, 1, 5, 31),
                    content = Content(text = "Bla bla bla bla"),
                    externalId = null,
                )

            val deduplicatedMessages =
                messageDeduplicationService.execute(
                    chatId = chatId,
                    messages = listOf(this),
                )
            Assertions.assertTrue(deduplicatedMessages.isEmpty())
        }

        NewMessageInput(
            authorName = "Beltrano",
            createdAt = LocalDateTime.of(2020, 1, 1, 5, 30),
            chatId = chatId,
            content = "Bla bla bla bla",
        ).apply {
            every { chatRepository.findLastMessageByChatId(chatId) } returns
                Message(
                    author = Author(name = "Beltrano", type = AuthorType.USER),
                    createdAt = LocalDateTime.of(2020, 1, 1, 5, 31),
                    content = Content(text = "Bla bla bla bla"),
                    externalId = null,
                )

            val deduplicatedMessages =
                messageDeduplicationService.execute(
                    chatId = chatId,
                    messages = listOf(this),
                )
            Assertions.assertTrue(deduplicatedMessages.isEmpty())
        }
    }

    @Test
    fun `should cut off olders messages when found message equal stored message when binary searching`() {
        val now = LocalDateTime.of(2023, 1, 1, 1, 1)
        val messages =
            (0..10000).map {
                NewMessageInput(
                    authorName = "Beltrano",
                    createdAt = now.plusMinutes(it.toLong()),
                    chatId = chatId,
                    content = "Bla bla bla bla",
                )
            }

        val expected = messages.subList(23, messages.size).toTypedArray()

        every { chatRepository.findLastMessageByChatId(chatId) } returns
            Message(
                author = Author(name = "Beltrano", type = AuthorType.USER),
                createdAt = LocalDateTime.of(2023, 1, 1, 1, 23),
                content = Content(text = "Bla bla bla bla"),
                externalId = null,
            )

        val deduplicatedMessages =
            messageDeduplicationService.execute(
                chatId = chatId,
                messages = messages,
            )

        Assertions.assertArrayEquals(expected, deduplicatedMessages.toTypedArray())
    }

    @Test
    fun `should cut off olders messages when binary searching`() {
        val now = LocalDateTime.of(2023, 1, 1, 1, 1)
        val messages =
            (0..10000).map {
                NewMessageInput(
                    authorName = "Beltrano",
                    createdAt = now.plusMinutes(it.toLong()),
                    chatId = chatId,
                    content = "Bla bla bla bla",
                )
            }

        val expected = messages.subList(22, messages.size).toTypedArray()

        every { chatRepository.findLastMessageByChatId(chatId) } returns
            Message(
                author = Author(name = "Beltrano", type = AuthorType.USER),
                createdAt = LocalDateTime.of(2023, 1, 1, 1, 23),
                content = Content(text = "Bla bla bla bla bla"),
                externalId = null,
            )

        val deduplicatedMessages =
            messageDeduplicationService.execute(
                chatId = chatId,
                messages = messages,
            )

        Assertions.assertArrayEquals(expected, deduplicatedMessages.toTypedArray())
    }

    @Test
    fun `when first message is after last message saved should return all messages`() {
        val now = LocalDateTime.of(2023, 1, 1, 1, 2)
        val messages =
            (0..10000).map {
                NewMessageInput(
                    authorName = "Beltrano",
                    createdAt = now.plusMinutes(it.toLong()),
                    chatId = chatId,
                    content = "Bla bla bla bla",
                )
            }

        every { chatRepository.findLastMessageByChatId(chatId) } returns
            Message(
                author = Author(name = "Beltrano", type = AuthorType.USER),
                createdAt = LocalDateTime.of(2023, 1, 1, 1, 1),
                content = Content(text = "Bla bla bla bla bla"),
                externalId = null,
            )

        val deduplicatedMessages =
            messageDeduplicationService.execute(
                chatId = chatId,
                messages = messages,
            )

        Assertions.assertArrayEquals(messages.toTypedArray(), deduplicatedMessages.toTypedArray())
    }

    @Test
    fun `when only last messages is new should cut off olders`() {
        val now = LocalDateTime.of(2023, 1, 1, 1, 0)
        val messages =
            (0..10000).map {
                NewMessageInput(
                    authorName = "Beltrano",
                    createdAt = now.plusMinutes(it.toLong()),
                    chatId = chatId,
                    content = "Bla bla bla bla",
                )
            }

        every { chatRepository.findLastMessageByChatId(chatId) } returns
            Message(
                author = Author(name = "Beltrano", type = AuthorType.USER),
                createdAt = now.plusMinutes(10000 - 10),
                content = Content(text = "Bla bla bla bla bla"),
                externalId = null,
            )

        val expected = messages.subList(10000 - 10, messages.size).toTypedArray()

        val deduplicatedMessages =
            messageDeduplicationService.execute(
                chatId = chatId,
                messages = messages,
            )

        Assertions.assertArrayEquals(expected, deduplicatedMessages.toTypedArray())
    }
}
