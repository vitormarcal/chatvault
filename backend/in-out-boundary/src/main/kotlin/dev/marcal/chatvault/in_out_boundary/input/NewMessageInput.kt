package dev.marcal.chatvault.in_out_boundary.input

import java.time.LocalDateTime

data class NewMessageInput(
    val authorName: String,
    val chatId: Long,
    val externalId: String? = null,
    val createdAt: LocalDateTime? = null,
    val content: String,
    val attachment: NewAttachmentInput? = null
)

data class NewMessagePayloadInput(
    val chatId: Long,
    val eventSource: Boolean,
    val messages: List<NewMessageInput>
)

data class NewAttachmentInput(
    val name: String,
    val content: String
)
