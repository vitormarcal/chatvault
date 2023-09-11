package dev.marcal.chatvault.repository

import dev.marcal.chatvault.model.Chat
import dev.marcal.chatvault.model.ChatBucketInfo
import dev.marcal.chatvault.model.ChatPayload
import dev.marcal.chatvault.model.MessagePayload

interface ChatRepository {

    fun saveNewMessages(payload: MessagePayload)
    fun findChatBucketInfoByChatId(chatId: Long): ChatBucketInfo?

    fun create(payload: ChatPayload): ChatBucketInfo
    fun existsByExternalId(externalId: String): Boolean
    fun findChatBucketInfoByExternalId(externalId: String): ChatBucketInfo
}