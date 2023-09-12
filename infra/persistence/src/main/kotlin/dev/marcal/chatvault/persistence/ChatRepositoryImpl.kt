package dev.marcal.chatvault.persistence

import com.fasterxml.jackson.databind.ObjectMapper
import dev.marcal.chatvault.model.*
import dev.marcal.chatvault.persistence.entity.*
import dev.marcal.chatvault.persistence.repository.ChatCrudRepository
import dev.marcal.chatvault.persistence.repository.EventSourceCrudRepository
import dev.marcal.chatvault.persistence.repository.MessageCrudRepository
import dev.marcal.chatvault.repository.ChatRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.jvm.optionals.getOrNull

@Service
class ChatRepositoryImpl(
    private val chatCrudRepository: ChatCrudRepository,
    private val messageCrudRepository: MessageCrudRepository,
    private val eventSourceCrudRepository: EventSourceCrudRepository,
    private val objectMapper: ObjectMapper
) : ChatRepository {

    @Transactional
    override fun saveNewMessages(payload: MessagePayload, eventSource: Boolean) {
        if (eventSource) {
            saveNewMessageEventSource(payload)
            return
        }
        val messagesToSave = payload.toMessagesEntity()
        messageCrudRepository.saveAll(messagesToSave)
    }

    private fun saveNewMessageEventSource(payload: MessagePayload) {
        val messagesToSave = payload.toEventSourceEntity(objectMapper)
        eventSourceCrudRepository.saveAll(messagesToSave)
    }

    override fun create(payload: ChatPayload): ChatBucketInfo {
        return chatCrudRepository.save(payload.toChatEntity()).toChatBucketInfo()
    }
    override fun findChatBucketInfoByChatId(chatId: Long): ChatBucketInfo? {
        return chatCrudRepository.findById(chatId).getOrNull()?.toChatBucketInfo()
    }

    override fun existsByExternalId(externalId: String): Boolean {
        return chatCrudRepository.existsByExternalId(externalId)
    }

    override fun findChatBucketInfoByExternalId(externalId: String): ChatBucketInfo {
        return chatCrudRepository.findByExternalId(externalId).toChatBucketInfo()
    }
}