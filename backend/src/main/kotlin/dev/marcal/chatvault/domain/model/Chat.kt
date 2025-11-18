package dev.marcal.chatvault.domain.model

import java.io.InputStream

data class Chat(
    val id: Long,
    val externalId: String? = null,
    val name: String,
    val messages: List<Message> = emptyList(),
    val bucket: Bucket,
)

data class Author(
    val name: String,
    val type: AuthorType,
)

enum class AuthorType {
    SYSTEM,
    USER,
}

data class Content(
    val text: String,
    val attachment: Attachment? = null,
)

data class Attachment(
    val name: String,
    val bucket: Bucket,
) {
    fun toBucketFile(inputStream: InputStream? = null): BucketFile =
        BucketFile(stream = inputStream, fileName = this.name, address = bucket)

    fun toBucketFile(bytes: ByteArray): BucketFile = BucketFile(bytes = bytes, fileName = this.name, address = bucket)
}

data class Bucket(
    val path: String,
) {
    fun withPath(path: String): Bucket = this.copy(path = this.path + path)

    fun toBucketFile(): BucketFile = BucketFile(fileName = "/", address = this)
}
