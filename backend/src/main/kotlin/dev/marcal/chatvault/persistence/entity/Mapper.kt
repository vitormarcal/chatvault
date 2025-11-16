package dev.marcal.chatvault.persistence.entity

import com.fasterxml.jackson.databind.ObjectMapper
import dev.marcal.chatvault.domain.model.Attachment
import dev.marcal.chatvault.domain.model.Author
import dev.marcal.chatvault.domain.model.AuthorType
import dev.marcal.chatvault.domain.model.Bucket
import dev.marcal.chatvault.domain.model.ChatBucketInfo
import dev.marcal.chatvault.domain.model.ChatPayload
import dev.marcal.chatvault.domain.model.Content
import dev.marcal.chatvault.domain.model.Message
import dev.marcal.chatvault.domain.model.MessagePayload

fun MessagePayload.toMessagesEntity(): List<MessageEntity> =
    this.messages.map {
        MessageEntity(
            author = it.author.name,
            authorType = it.author.type.name,
            content = it.content.text,
            attachmentPath =
                it.content.attachment
                    ?.bucket
                    ?.path,
            attachmentName = it.content.attachment?.name,
            chatId = this.chatId,
            externalId = it.externalId,
            createdAt = it.createdAt,
        )
    }

fun MessagePayload.toEventSourceEntity(objectMapper: ObjectMapper): List<EventSourceEntity> =
    this.messages.map {
        val hasAttachment = it.content.attachment != null
        EventSourceEntity(
            chatId = this.chatId,
            externalId = it.externalId,
            createdAt = it.createdAt,
            messageImported = false,
            attachmentImported = if (hasAttachment) false else null,
            hasAttachment = hasAttachment,
            payload = objectMapper.writeValueAsString(it),
        )
    }

fun EventSourceEntity.toMessage(objectMapper: ObjectMapper): Message = objectMapper.readValue(this.payload, Message::class.java)

fun ChatPayload.toChatEntity(): ChatEntity =
    ChatEntity(
        name = this.name,
        externalId = this.externalId,
        bucket = this.bucket.path,
    )

fun MessageEntity.toMessageDomain() =
    Message(
        author = this.toAuthorDomain(),
        createdAt = this.createdAt,
        externalId = this.externalId,
        content = this.toContentDomain(),
    )

fun MessageEntity.toAuthorDomain() = Author(name = this.author, type = AuthorType.valueOf(this.authorType))

fun MessageEntity.toContentDomain() = Content(text = this.content, attachment = this.toAttachmentDomain())

fun MessageEntity.toAttachmentDomain() =
    this.attachmentName?.let {
        Attachment(name = it, bucket = this.toBucketDomain())
    }

fun MessageEntity.toBucketDomain() = Bucket(path = requireNotNull(this.attachmentPath))

fun ChatEntity.toChatBucketInfo() = ChatBucketInfo(chatId = this.id!!, Bucket(this.bucket))
