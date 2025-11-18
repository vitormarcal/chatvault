package dev.marcal.chatvault.domain.model

import java.time.LocalDateTime

data class Message(
    val author: Author,
    val createdAt: LocalDateTime,
    val content: Content,
    val externalId: String?,
) {
    companion object {
        fun create(
            chatBucketInfo: ChatBucketInfo,
            authorName: String,
            content: String,
            createdAt: LocalDateTime? = null,
            externalId: String? = null,
            attachmentName: String? = null,
            now: () -> LocalDateTime = { LocalDateTime.now() },
        ): Message {
            val authorType =
                if (authorName.isEmpty()) AuthorType.SYSTEM else AuthorType.USER

            val attachment =
                attachmentName?.let {
                    Attachment(
                        name = it,
                        bucket = chatBucketInfo.bucket.withPath("/"),
                    )
                }

            return Message(
                author = Author(name = authorName, type = authorType),
                createdAt = createdAt ?: now(),
                externalId = externalId,
                content = Content(text = content, attachment = attachment),
            )
        }
    }
}
