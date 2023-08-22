package dev.marcal.chatvault.model

import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDateTime


data class Chat(
    val id: Long,
    val messages: List<Message> = emptyList(),
    val bucket: Bucket
)

data class Message(
    val author: Author,
    val createdAt: LocalDateTime,
    val content: Content
)

data class Author(
    val name: String,
    val type: AuthorType
)

enum class AuthorType {
    SYSTEM,
    USER
}

data class Content(
    val text: String,
    val attachment: Attachment? = null
)

data class Attachment(
    val name: String,
    val bucket: Bucket,
)

data class Bucket(
    val path: String
) {
    fun withPath(path: String): Bucket {
        return this.copy(path = this.path + path)
    }
}
