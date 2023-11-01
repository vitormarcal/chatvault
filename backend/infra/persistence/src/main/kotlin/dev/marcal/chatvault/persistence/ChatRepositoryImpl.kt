package dev.marcal.chatvault.persistence

import com.fasterxml.jackson.databind.ObjectMapper
import dev.marcal.chatvault.in_out_boundary.output.MessageOutput
import dev.marcal.chatvault.model.*
import dev.marcal.chatvault.persistence.dto.toChatLastMessage
import dev.marcal.chatvault.persistence.entity.*
import dev.marcal.chatvault.persistence.repository.ChatCrudRepository
import dev.marcal.chatvault.persistence.repository.EventSourceCrudRepository
import dev.marcal.chatvault.persistence.repository.MessageCrudRepository
import dev.marcal.chatvault.repository.ChatRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
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

    override fun findLegacyToImport(chatId: Long, page: Int, size: Int): Page<Message> {
        return eventSourceCrudRepository.findLegacyMessageNotImportedByChatId(
            chatId,
            PageRequest.of(page - 1, size, Sort.by(Sort.Order.asc("externalId")))
        )
            .let { pageRequest ->
                Page(
                    data = pageRequest.map { it.toMessage(objectMapper) }.toList(),
                    page = page,
                    totalPages = pageRequest.totalPages,
                    items = size,
                    totalItems = pageRequest.totalElements
                )
            }

    }

    override fun findAttachmentLegacyToImport(chatId: Long, page: Int, size: Int): Page<Message> {
        return eventSourceCrudRepository.findLegacyAttachmentNotImportedByChatId(
            chatId,
            PageRequest.of(page - 1, size, Sort.by(Sort.Order.asc("externalId")))
        )
            .let { pageRequest ->
                Page(
                    data = pageRequest.map { it.toMessage(objectMapper) }.toList(),
                    page = page,
                    totalPages = pageRequest.totalPages,
                    items = size,
                    totalItems = pageRequest.totalElements
                )
            }

    }

    override fun findAllEventSourceChatId(): List<Long> {
        return eventSourceCrudRepository.findAllChatId()
    }

    @Transactional
    override fun saveLegacyMessage(messagePayload: MessagePayload) {
        saveNewMessages(messagePayload, eventSource = false)
        messagePayload.messages
            .map { requireNotNull(it.externalId) }
            .forEach { eventSourceCrudRepository.setImportedTrue(it) }

    }

    override fun setLegacyAttachmentImported(messageExternalId: String) {
        eventSourceCrudRepository.setAttachmentImportedTrue(messageExternalId)
    }

    override fun findAllChatsWithLastMessage(): Sequence<ChatLastMessage> {
        return chatCrudRepository.findAllChatsWithLastMessage().asSequence().map { it.toChatLastMessage() }
    }

    override fun findMessagesBy(chatId: Long, pageable: Pageable): org.springframework.data.domain.Page<MessageOutput> {
        return messageCrudRepository.findAllByChatIdIs(chatId, pageable)
    }

    override fun countChatMessages(chatId: Long): Long {
        return messageCrudRepository.countByChatId(chatId)
    }

    override fun findMessageBy(chatId: Long, messageId: Long): Message? {
        return messageCrudRepository.findMessageEntityByIdAndChatId(id = messageId, chatId = chatId)?.toMessageDomain()
    }

    override fun findChatBucketInfoByChatName(chatName: String): ChatBucketInfo? {
        return chatCrudRepository.findByName(chatName)?.toChatBucketInfo()
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