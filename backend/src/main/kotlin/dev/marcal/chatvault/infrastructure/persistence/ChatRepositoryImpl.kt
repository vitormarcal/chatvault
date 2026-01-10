package dev.marcal.chatvault.infrastructure.persistence

import dev.marcal.chatvault.domain.model.AttachmentSummary
import dev.marcal.chatvault.domain.model.ChatBucketInfo
import dev.marcal.chatvault.domain.model.ChatLastMessage
import dev.marcal.chatvault.domain.model.ChatPayload
import dev.marcal.chatvault.domain.model.Message
import dev.marcal.chatvault.domain.model.MessagePayload
import dev.marcal.chatvault.domain.repository.ChatRepository
import dev.marcal.chatvault.infrastructure.persistence.dto.toChatLastMessage
import dev.marcal.chatvault.infrastructure.persistence.entity.toChatBucketInfo
import dev.marcal.chatvault.infrastructure.persistence.entity.toChatEntity
import dev.marcal.chatvault.infrastructure.persistence.entity.toMessageDomain
import dev.marcal.chatvault.infrastructure.persistence.entity.toMessagesEntity
import dev.marcal.chatvault.infrastructure.persistence.repository.ChatCrudRepository
import dev.marcal.chatvault.infrastructure.persistence.repository.MessageCrudRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.jvm.optionals.getOrNull

@Service
class ChatRepositoryImpl(
    private val chatCrudRepository: ChatCrudRepository,
    private val messageCrudRepository: MessageCrudRepository,
) : ChatRepository {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    @Transactional
    override fun saveNewMessages(payload: MessagePayload) {
        val messagesToSave = payload.toMessagesEntity()
        messageCrudRepository.saveAll(messagesToSave)
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

    override fun findMessagesAroundDate(
        chatId: Long,
        targetDate: java.time.LocalDateTime,
        pageable: Pageable,
    ): org.springframework.data.domain.Page<Message> =
        messageCrudRepository
            .findFirstMessageAtOrAfterDate(
                chatId = chatId,
                targetDate = targetDate,
                pageable = pageable,
            ).map { it.toMessageDomain() }
}
