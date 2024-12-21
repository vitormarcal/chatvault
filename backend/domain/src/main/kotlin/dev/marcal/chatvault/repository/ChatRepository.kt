package dev.marcal.chatvault.repository

import dev.marcal.chatvault.in_out_boundary.output.AttachmentInfoOutput
import dev.marcal.chatvault.in_out_boundary.output.MessageOutput
import dev.marcal.chatvault.model.*
import org.springframework.data.domain.Pageable

interface ChatRepository {

    fun saveNewMessages(payload: MessagePayload, eventSource: Boolean = false)
    fun findChatBucketInfoByChatId(chatId: Long): ChatBucketInfo?

    fun create(payload: ChatPayload): ChatBucketInfo
    fun existsByExternalId(externalId: String): Boolean
    fun existsByChatId(chatId: Long): Boolean
    fun findChatBucketInfoByExternalId(externalId: String): ChatBucketInfo
    fun findLegacyToImport(chatId: Long, page: Int, size: Int): Page<Message>
    fun findAttachmentLegacyToImport(chatId: Long, page: Int, size: Int): Page<Message>
    fun findAllEventSourceChatId(): List<Long>
    fun saveLegacyMessage(messagePayload: MessagePayload)

    fun setLegacyAttachmentImported(messageExternalId: String)

    fun findAllChatsWithLastMessage(): Sequence<ChatLastMessage>

    fun findMessagesBy(chatId: Long, query: String? =  null, pageable: Pageable): org.springframework.data.domain.Page<MessageOutput>
    fun findMessageBy(chatId: Long, messageId: Long): Message?
    fun findChatBucketInfoByChatName(chatName: String): ChatBucketInfo?
    fun countChatMessages(chatId: Long): Long
    fun setChatNameByChatId(chatId: Long, chatName: String)
    fun findAttachmentMessageIdsByChatId(chatId: Long): Sequence<AttachmentInfoOutput>
    fun findLastMessageByChatId(chatId: Long): Message?
    fun deleteChat(chatId: Long)
}