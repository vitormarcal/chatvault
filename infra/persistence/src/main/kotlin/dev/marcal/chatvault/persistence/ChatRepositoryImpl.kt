package dev.marcal.chatvault.persistence

import dev.marcal.chatvault.model.*
import dev.marcal.chatvault.persistence.entity.toChatEntity
import dev.marcal.chatvault.persistence.entity.toMessagesEntity
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
    override fun saveNewMessages(payload: MessagePayload) {
        val messagesToSave = payload.toMessagesEntity()
        messageCrudRepository.saveAll(messagesToSave)
    }

    override fun create(payload: ChatPayload) {
        chatCrudRepository.save(payload.toChatEntity())
    }
    override fun findChatBucketInfoByChatId(chatId: Long): ChatBucketInfo? {
        return chatCrudRepository.findById(chatId).getOrNull()?.let {
            ChatBucketInfo(chatId = it.id!!, Bucket(it.bucket))
        }
    }

    override fun existsByExternalId(externalId: String): Boolean {
        return chatCrudRepository.existsByExternalId(externalId)
    }
}