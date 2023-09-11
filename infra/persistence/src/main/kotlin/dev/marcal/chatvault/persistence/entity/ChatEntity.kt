package dev.marcal.chatvault.persistence.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "chat", uniqueConstraints = [
        UniqueConstraint(name = "chat_external_id_key", columnNames = ["externalId"])
    ]
)
data class ChatEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Long? = null,
    val name: String,
    val externalId: String? = null,
    val bucket: String
)

@Entity
@Table(
    name = "message", uniqueConstraints = [
        UniqueConstraint(name = "message_external_id_key", columnNames = ["externalId"])
    ]
)
data class MessageEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Long? = null,
    val author: String,
    val authorType: String,
    val createdAt: LocalDateTime,
    @Column(columnDefinition = "TEXT") val content: String,
    val attachmentPath: String?,
    val chatId: Long,
    val externalId: String? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chatId", insertable = false, updatable = false)
    val chat: ChatEntity? = null
)
