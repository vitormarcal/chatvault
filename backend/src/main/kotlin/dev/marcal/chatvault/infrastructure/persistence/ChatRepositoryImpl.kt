package dev.marcal.chatvault.infrastructure.persistence

import com.fasterxml.jackson.databind.ObjectMapper
import dev.marcal.chatvault.domain.model.AttachmentSummary
import dev.marcal.chatvault.domain.model.ChatBucketInfo
import dev.marcal.chatvault.domain.model.ChatLastMessage
import dev.marcal.chatvault.domain.model.ChatPayload
import dev.marcal.chatvault.domain.model.Message
import dev.marcal.chatvault.domain.model.MessagePayload
import dev.marcal.chatvault.domain.model.Page
import dev.marcal.chatvault.domain.repository.ChatRepository
import dev.marcal.chatvault.infrastructure.persistence.dto.toChatLastMessage
import dev.marcal.chatvault.infrastructure.persistence.entity.toChatBucketInfo
import dev.marcal.chatvault.infrastructure.persistence.entity.toChatEntity
import dev.marcal.chatvault.infrastructure.persistence.entity.toEventSourceEntity
import dev.marcal.chatvault.infrastructure.persistence.entity.toMessage
import dev.marcal.chatvault.infrastructure.persistence.entity.toMessageDomain
import dev.marcal.chatvault.infrastructure.persistence.entity.toMessagesEntity
import dev.marcal.chatvault.infrastructure.persistence.repository.ChatCrudRepository
import dev.marcal.chatvault.infrastructure.persistence.repository.EventSourceCrudRepository
import dev.marcal.chatvault.infrastructure.persistence.repository.MessageCrudRepository
import org.slf4j.LoggerFactory
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
    private val objectMapper: ObjectMapper,
) : ChatRepository {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    @Transactional
    override fun saveNewMessages(
        payload: MessagePayload,
        eventSource: Boolean,
    ) {
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

    override fun findLegacyToImport(
        chatId: Long,
        page: Int,
        size: Int,
    ): Page<Message> =
        eventSourceCrudRepository
            .findLegacyMessageNotImportedByChatId(
                chatId,
                PageRequest.of(page - 1, size, Sort.by(Sort.Order.asc("externalId"))),
            ).let { pageRequest ->
                Page(
                    data = pageRequest.map { it.toMessage(objectMapper) }.toList(),
                    page = page,
                    totalPages = pageRequest.totalPages,
                    items = size,
                    totalItems = pageRequest.totalElements,
                )
            }

    override fun findAttachmentLegacyToImport(
        chatId: Long,
        page: Int,
        size: Int,
    ): Page<Message> =
        eventSourceCrudRepository
            .findLegacyAttachmentNotImportedByChatId(
                chatId,
                PageRequest.of(page - 1, size, Sort.by(Sort.Order.asc("externalId"))),
            ).let { pageRequest ->
                Page(
                    data = pageRequest.map { it.toMessage(objectMapper) }.toList(),
                    page = page,
                    totalPages = pageRequest.totalPages,
                    items = size,
                    totalItems = pageRequest.totalElements,
                )
            }

    override fun findAllEventSourceChatId(): List<Long> = eventSourceCrudRepository.findAllChatId()

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

    override fun findAllChatsWithLastMessage(): Sequence<ChatLastMessage> =
        chatCrudRepository.findAllChatsWithLastMessage().asSequence().map {
            it.toChatLastMessage()
        }

    override fun findMessagesBy(
        chatId: Long,
        query: String?,
        pageable: Pageable,
    ): org.springframework.data.domain.Page<Message> =
        messageCrudRepository
            .findAllByChatIdIs(
                chatId = chatId,
                query = query ?: "",
                pageable = pageable,
            ).map { it.toMessageDomain() }

    override fun findAttachmentMessageIdsByChatId(chatId: Long): Sequence<AttachmentSummary> =
        messageCrudRepository
            .findMessageIdByChatIdAndAttachmentExists(chatId)
            .asSequence()
            .map { AttachmentSummary(id = it.messageId, name = it.name) }

    override fun findLastMessageByChatId(chatId: Long): Message? =
        messageCrudRepository.findTopByChatIdOrderByIdDesc(chatId)?.toMessageDomain()

    @Transactional
    override fun deleteChat(chatId: Long) {
        logger.info("start to remove messages from $chatId")
        messageCrudRepository.deleteAllByChatId(chatId)
        logger.info("start to remove chat from $chatId")
        chatCrudRepository.deleteById(chatId)
    }

    override fun countChatMessages(chatId: Long): Long = messageCrudRepository.countByChatId(chatId)

    @Transactional
    override fun setChatNameByChatId(
        chatId: Long,
        chatName: String,
    ) {
        chatCrudRepository.updateChatNameByChatId(chatName = chatName, chatId = chatId)
    }

    override fun findMessageBy(
        chatId: Long,
        messageId: Long,
    ): Message? = messageCrudRepository.findMessageEntityByIdAndChatId(id = messageId, chatId = chatId)?.toMessageDomain()

    override fun findChatBucketInfoByChatName(chatName: String): ChatBucketInfo? =
        chatCrudRepository.findByName(chatName)?.toChatBucketInfo()

    override fun create(payload: ChatPayload): ChatBucketInfo = chatCrudRepository.save(payload.toChatEntity()).toChatBucketInfo()

    override fun findChatBucketInfoByChatId(chatId: Long): ChatBucketInfo? =
        chatCrudRepository.findById(chatId).getOrNull()?.toChatBucketInfo()

    override fun existsByExternalId(externalId: String): Boolean = chatCrudRepository.existsByExternalId(externalId)

    override fun existsByChatId(chatId: Long): Boolean = chatCrudRepository.existsById(chatId)

    override fun findChatBucketInfoByExternalId(externalId: String): ChatBucketInfo =
        chatCrudRepository.findByExternalId(externalId).toChatBucketInfo()
}
