package dev.marcal.chatvault.model

import java.time.LocalDateTime


data class Chat(
    val id: Long,
    val externalId: String? = null,
    val name: String,
    val messages: List<Message> = emptyList(),
    val bucket: Bucket
)

data class Message(
    val author: Author,
    val createdAt: LocalDateTime,
    val content: Content,
    val externalId: String?
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
) {
    fun toBucketFile(bytes: ByteArray): BucketFile {
        return BucketFile(bytes = bytes, fileName = this.name, address = bucket)
    }
}

data class Bucket(
    val path: String
) {
    fun withPath(path: String): Bucket {
        return this.copy(path = this.path + path)
    }
}
