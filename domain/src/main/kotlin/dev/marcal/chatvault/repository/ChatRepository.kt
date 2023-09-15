package dev.marcal.chatvault.repository

import dev.marcal.chatvault.model.*

interface ChatRepository {

    fun saveNewMessages(payload: MessagePayload, eventSource: Boolean = false)
    fun findChatBucketInfoByChatId(chatId: Long): ChatBucketInfo?

    fun create(payload: ChatPayload): ChatBucketInfo
    fun existsByExternalId(externalId: String): Boolean
    fun findChatBucketInfoByExternalId(externalId: String): ChatBucketInfo
    fun findLegacyToImport(chatId: Long, page: Int, size: Int): Page<Message>
    fun findAttachmentLegacyToImport(chatId: Long, page: Int, size: Int): Page<Message>
    fun findAllEventSourceChatId(): List<Long>
    fun saveLegacyMessage(messagePayload: MessagePayload)

    fun setLegacyAttachmentImported(messageExternalId: String)

    fun findAllChatsWithLastMessage(): Sequence<ChatLastMessage>
}