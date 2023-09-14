package dev.marcal.chatvault.app_service.dto

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDateTime

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
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    val date: LocalDateTime,
    val createdAt: String,
    val updateAt: String?
)
