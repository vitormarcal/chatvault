package dev.marcal.chatvault.domain.model

import java.time.LocalDateTime

data class ChatLastMessage(
    val chatId: Long,
    val chatName: String,
    val author: Author,
    val content: String,
    val msgCreatedAt: LocalDateTime,
    val msgCount: Long,
)
