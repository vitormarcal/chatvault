package dev.marcal.chatvault.application.message

import dev.marcal.chatvault.api.dto.input.NewMessageInput
import dev.marcal.chatvault.api.dto.input.NewMessagePayloadInput
import dev.marcal.chatvault.api.web.exception.ChatNotFoundException
import dev.marcal.chatvault.domain.model.Attachment
import dev.marcal.chatvault.domain.model.Author
import dev.marcal.chatvault.domain.model.AuthorType
import dev.marcal.chatvault.domain.model.ChatBucketInfo
import dev.marcal.chatvault.domain.model.Content
import dev.marcal.chatvault.domain.model.Message
import dev.marcal.chatvault.domain.model.MessagePayload
import dev.marcal.chatvault.domain.repository.ChatRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class MessageCreatorService(
    private val chatRepository: ChatRepository,
    private val messageDeduplicationService: MessageDeduplicationService,
) {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    fun execute(input: NewMessageInput) {
        val payloadInput =
            NewMessagePayloadInput(
                chatId = input.chatId,
                messages = listOf(input),
                eventSource = false,
            )
        execute(payloadInput)
    }

    fun execute(input: NewMessagePayloadInput) {
        val chatBucketInfo =
            chatRepository.findChatBucketInfoByChatId(input.chatId)
                ?: throw ChatNotFoundException("Unable to create a message because the chat ${input.chatId} was not found")

        val theMessagePayload =
            MessagePayload(
                chatId = chatBucketInfo.chatId,
                messages =
                    input.messages
                        .let { messageDeduplicationService.execute(chatBucketInfo.chatId, it) }
                        .map { buildNewMessage(it, chatBucketInfo) },
            )

        if (theMessagePayload.messages.isEmpty()) {
            throw IllegalStateException("there are no messages to create, message list is empty $theMessagePayload")
        }

        logger.info("try to save ${theMessagePayload.messages.size} messages, chatInfo=$chatBucketInfo")
        chatRepository.saveNewMessages(payload = theMessagePayload, eventSource = input.eventSource)
    }

    private fun buildNewMessage(
        input: NewMessageInput,
        chatBucketInfo: ChatBucketInfo,
    ): Message {
        require(input.chatId == chatBucketInfo.chatId)
        return Message(
            author =
                Author(
                    name = input.authorName,
                    type = if (input.authorName.isEmpty()) AuthorType.SYSTEM else AuthorType.USER,
                ),
            createdAt = input.createdAt ?: LocalDateTime.now(),
            externalId = input.externalId,
            content =
                Content(
                    text = input.content,
                    attachment =
                        input.attachment?.let {
                            Attachment(
                                name = it.name,
                                bucket = chatBucketInfo.bucket.withPath("/"),
                            )
                        },
                ),
        )
    }
}
