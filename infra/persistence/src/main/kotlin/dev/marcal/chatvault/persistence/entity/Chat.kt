package dev.marcal.chatvault.persistence.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "chat")
data class Chat(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Long? = null,
    val name: String,
    val bucket: String
)

@Entity
@Table(name = "message")
data class Message(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Long? = null,
    val author: String,
    val authorType: String,
    val createdAt: LocalDateTime,
    val content: String,
    val attachmentPath: String?,
    val chatId: Long,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chatId", insertable = false, updatable = false)
    val chat: Chat? = null
)
