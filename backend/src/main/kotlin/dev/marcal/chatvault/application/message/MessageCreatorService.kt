package dev.marcal.chatvault.application.message

import dev.marcal.chatvault.api.dto.input.NewMessageInput
import dev.marcal.chatvault.api.dto.input.NewMessagePayloadInput
import dev.marcal.chatvault.api.dto.mapper.toMessageDomain
import dev.marcal.chatvault.api.web.exception.ChatNotFoundException
import dev.marcal.chatvault.domain.model.MessagePayload
import dev.marcal.chatvault.domain.repository.ChatRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

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

        val messagesDomain =
            input.messages
                .let { messageDeduplicationService.execute(chatBucketInfo.chatId, it) }
                .map { it.toMessageDomain(chatBucketInfo) }

        if (messagesDomain.isEmpty()) {
            throw IllegalStateException("there are no messages to create, message list is empty for chatId=${chatBucketInfo.chatId}")
        }

        val payload =
            MessagePayload(
                chatId = chatBucketInfo.chatId,
                messages = messagesDomain,
            )

        logger.info("try to save ${payload.messages.size} messages, chatInfo=$chatBucketInfo")
        chatRepository.saveNewMessages(payload = payload, eventSource = input.eventSource)
    }
}
