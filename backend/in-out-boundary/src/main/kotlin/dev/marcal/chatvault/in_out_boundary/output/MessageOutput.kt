package dev.marcal.chatvault.in_out_boundary.output

import java.time.LocalDateTime

data class MessageOutput(
    val id: Long? = null,
    val author: String? = null,
    val authorType: String,
    val content: String,
    val attachmentName: String? = null,
    val createdAt: LocalDateTime
)