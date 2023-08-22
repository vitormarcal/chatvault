package dev.marcal.chatvault.repository

import dev.marcal.chatvault.model.Chat
import dev.marcal.chatvault.model.ChatBucketInfo

interface ChatRepository {

    fun saveNewMessages(chat: Chat)
    fun findChatBucketInfoByChatId(chatId: Long): ChatBucketInfo?

}