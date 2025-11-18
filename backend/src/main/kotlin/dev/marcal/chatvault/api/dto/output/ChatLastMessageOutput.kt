package dev.marcal.chatvault.api.dto.output

import java.time.LocalDateTime

data class ChatLastMessageOutput(
    val chatId: Long,
    val chatName: String,
    val authorName: String,
    val authorType: String,
    val content: String,
    val msgCreatedAt: LocalDateTime,
    val msgCount: Long,
)
