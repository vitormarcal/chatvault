package dev.marcal.chatvault.in_out_boundary.output

import java.time.LocalDateTime

data class ChatLastMessageOutput(
    val chatId: Long,
    val chatName: String,
    val authorName: String,
    val authorType: String,
    val content: String,
    val msgCreatedAt: LocalDateTime,
    val msgCount: Long
)