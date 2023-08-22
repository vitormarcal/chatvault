package dev.marcal.chatvault.usecase

import dev.marcal.chatvault.model.*
import dev.marcal.chatvault.repository.ChatRepository
import dev.marcal.chatvault.service.NewMessage
import dev.marcal.chatvault.service.input.NewMessageInput
import org.springframework.stereotype.Service

@Service
class NewMessageUseCase(
    private val chatRepository: ChatRepository
) : NewMessage {
    override fun execute(input: NewMessageInput) {

        val chatBucketInfo =
            chatRepository.findChatBucketInfoByChatId(input.chatId) ?: throw RuntimeException("chat not found")

        val theChat = Chat(
            id = chatBucketInfo.chatId,
            bucket = chatBucketInfo.bucket,
            messages = listOf(buildNewMessage(input, chatBucketInfo))
        )

        chatRepository.saveNewMessages(chat = theChat)
    }

    private fun buildNewMessage(input: NewMessageInput, chatBucketInfo: ChatBucketInfo): Message {
        return Message(
            author = Author(
                name = input.authorName,
                type = if (input.authorName.isEmpty()) AuthorType.SYSTEM else AuthorType.USER
            ),
            createdAt = input.createdAt,
            content = Content(text = input.content, attachment = input.attachment?.let {
                Attachment(
                    name = it.name,
                    bucket = chatBucketInfo.bucket.withPath(input.createdAt.toLocalDate().toString())
                )
            })

        )
    }
}