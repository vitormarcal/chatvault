package dev.marcal.chatvault.persistence.entity

import com.fasterxml.jackson.databind.ObjectMapper
import dev.marcal.chatvault.model.*


fun MessagePayload.toMessagesEntity(): List<MessageEntity> {
    return this.messages.map {
        MessageEntity(
            author = it.author.name,
            authorType = it.author.type.name,
            content = it.content.text,
            attachmentPath = it.content.attachment?.bucket?.path,
            chatId = this.chatId,
            externalId = it.externalId,
            createdAt = it.createdAt
        )
    }
}

fun MessagePayload.toEventSourceEntity(objectMapper: ObjectMapper): List<EventSourceEntity> {
    return this.messages.map {
        val hasAttachment = it.content.attachment != null
        EventSourceEntity(
            chatId = this.chatId,
            externalId = it.externalId,
            createdAt = it.createdAt,
            messageImported = false,
            attachmentImported = if (hasAttachment) false else null,
            hasAttachment = hasAttachment,
            payload = objectMapper.writeValueAsString(it)
        )
    }
}

fun EventSourceEntity.toMessage(objectMapper: ObjectMapper): Message {
    return objectMapper.readValue(this.payload, Message::class.java)
}

fun ChatPayload.toChatEntity(): ChatEntity {
    return ChatEntity(
        name = this.name,
        externalId = this.externalId,
        bucket = this.bucket.path
    )
}

fun ChatEntity.toChatBucketInfo() = ChatBucketInfo(chatId = this.id!!, Bucket(this.bucket))