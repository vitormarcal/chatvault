package dev.marcal.chatvault.service.input

import java.time.LocalDateTime

data class NewMessageInput(
    val authorName: String,
    val chatId: Long,
    val createdAt: LocalDateTime,
    val content: String,
    val attachment: NewAttachmentInput? = null

)

data class NewAttachmentInput(
    val name: String,
    val content: String
)
