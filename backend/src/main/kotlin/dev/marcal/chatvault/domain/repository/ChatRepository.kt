package dev.marcal.chatvault.domain.repository

import dev.marcal.chatvault.domain.model.AttachmentSummary
import dev.marcal.chatvault.domain.model.ChatBucketInfo
import dev.marcal.chatvault.domain.model.ChatLastMessage
import dev.marcal.chatvault.domain.model.ChatPayload
import dev.marcal.chatvault.domain.model.Message
import dev.marcal.chatvault.domain.model.MessagePayload
import org.springframework.data.domain.Pageable

interface ChatRepository {
    fun saveNewMessages(payload: MessagePayload)

    fun findChatBucketInfoByChatId(chatId: Long): ChatBucketInfo?

    fun create(payload: ChatPayload): ChatBucketInfo

    fun existsByExternalId(externalId: String): Boolean

    fun existsByChatId(chatId: Long): Boolean

    fun findChatBucketInfoByExternalId(externalId: String): ChatBucketInfo

    fun findAllChatsWithLastMessage(): Sequence<ChatLastMessage>

    fun findMessagesBy(
        chatId: Long,
        query: String? = null,
        pageable: Pageable,
    ): org.springframework.data.domain.Page<Message>

    fun findMessageBy(
        chatId: Long,
        messageId: Long,
    ): Message?

    fun findChatBucketInfoByChatName(chatName: String): ChatBucketInfo?

    fun countChatMessages(chatId: Long): Long

    fun setChatNameByChatId(
        chatId: Long,
        chatName: String,
    )

    fun findAttachmentMessageIdsByChatId(chatId: Long): Sequence<AttachmentSummary>

    fun findLastMessageByChatId(chatId: Long): Message?

    fun deleteChat(chatId: Long)

    fun findMessagesAroundDate(
        chatId: Long,
        targetDate: java.time.LocalDateTime,
        pageable: Pageable,
    ): org.springframework.data.domain.Page<Message>
}
