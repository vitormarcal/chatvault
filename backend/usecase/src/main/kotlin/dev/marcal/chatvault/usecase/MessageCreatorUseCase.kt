package dev.marcal.chatvault.usecase

import dev.marcal.chatvault.in_out_boundary.input.NewMessageInput
import dev.marcal.chatvault.in_out_boundary.input.NewMessagePayloadInput
import dev.marcal.chatvault.in_out_boundary.output.exceptions.ChatNotFoundException
import dev.marcal.chatvault.model.*
import dev.marcal.chatvault.repository.ChatRepository
import dev.marcal.chatvault.service.MessageCreator
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class MessageCreatorUseCase(
    private val chatRepository: ChatRepository,
    private val messageDeduplicationUseCase: MessageDeduplicationUseCase
) : MessageCreator {

    private val logger = LoggerFactory.getLogger(this.javaClass)
    override fun execute(input: NewMessageInput) {
        val payloadInput = NewMessagePayloadInput(
            chatId = input.chatId,
            messages = listOf(input),
            eventSource = false,
        )
        execute(payloadInput)
    }

    override fun execute(input: NewMessagePayloadInput) {
        val chatBucketInfo =
            chatRepository.findChatBucketInfoByChatId(input.chatId) ?: throw ChatNotFoundException("Unable to create a message because the chat ${input.chatId} was not found")

        val theMessagePayload = MessagePayload(
            chatId = chatBucketInfo.chatId,
            messages = input.messages
                .let { messageDeduplicationUseCase.execute(chatBucketInfo.chatId, it) }
                .map { buildNewMessage(it, chatBucketInfo) }
        )

        if (theMessagePayload.messages.isEmpty()) {
            throw IllegalStateException("there are no messages to create, message list is empty $theMessagePayload")
        }

        logger.info("try to save ${theMessagePayload.messages.size} messages, chatInfo=${chatBucketInfo}")
        chatRepository.saveNewMessages(payload = theMessagePayload, eventSource = input.eventSource)
    }

    private fun buildNewMessage(input: NewMessageInput, chatBucketInfo: ChatBucketInfo): Message {
        require(input.chatId == chatBucketInfo.chatId)
        return Message(
            author = Author(
                name = input.authorName,
                type = if (input.authorName.isEmpty()) AuthorType.SYSTEM else AuthorType.USER
            ),
            createdAt = input.createdAt ?: LocalDateTime.now(),
            externalId = input.externalId,
            content = Content(text = input.content, attachment = input.attachment?.let {
                Attachment(
                    name = it.name,
                    bucket = chatBucketInfo.bucket.withPath("/")
                )
            })
        )
    }
}