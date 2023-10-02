package dev.marcal.chatvault.model

class ChatPayload(
    val externalId: String? = null,
    val name: String,
    val bucket: Bucket
)

data class MessagePayload(
    val chatId: Long,
    val messages: List<Message>
)