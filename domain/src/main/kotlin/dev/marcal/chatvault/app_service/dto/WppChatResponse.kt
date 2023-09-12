package dev.marcal.chatvault.app_service.dto

data class WppChatResponse<T>(
    val count: Long,
    val data: List<T>
)

data class ChatDTO(
    val id: Long,
    val name: String,
    val attachmentDir: String,
    val messagesCount: Int,
)

data class MessageDTO(
    val id: Long,
    val author: String?,
    val content: String,
    val attachmentName: String?,
    val date: String,
    val createdAt: String,
    val updateAt: String?
)
