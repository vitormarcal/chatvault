package dev.marcal.chatvault.persistence

import dev.marcal.chatvault.model.Bucket
import dev.marcal.chatvault.model.Chat
import dev.marcal.chatvault.model.ChatBucketInfo
import dev.marcal.chatvault.persistence.entity.Message
import dev.marcal.chatvault.persistence.repository.ChatCrudRepository
import dev.marcal.chatvault.persistence.repository.MessageCrudRepository
import dev.marcal.chatvault.repository.ChatRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.jvm.optionals.getOrNull

@Service
class ChatRepositoryImpl(
    private val chatCrudRepository: ChatCrudRepository,
    private val messageCrudRepository: MessageCrudRepository
) : ChatRepository {

    @Transactional
    override fun saveNewMessages(chat: Chat) {
        val messagesToSave = chat.messages.map {
            Message(
                author = it.author.name,
                authorType = it.author.type.name,
                content = it.content.text,
                attachmentPath = it.content.attachment?.bucket?.path,
                chatId = chat.id,
                createdAt = it.createdAt
            )
        }
        messageCrudRepository.saveAll(messagesToSave)
    }

    override fun findChatBucketInfoByChatId(chatId: Long): ChatBucketInfo? {
        return chatCrudRepository.findById(chatId).getOrNull()?.let {
            ChatBucketInfo(chatId = it.id!!, Bucket(it.bucket))
        }
    }
}