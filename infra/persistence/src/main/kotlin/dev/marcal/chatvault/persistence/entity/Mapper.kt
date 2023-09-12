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
        EventSourceEntity(
            chatId = this.chatId,
            externalId = it.externalId,
            createdAt = it.createdAt,
            payload = objectMapper.writeValueAsString(it)
        )
    }
}

fun ChatPayload.toChatEntity(): ChatEntity {
    return ChatEntity(
        name = this.name,
        externalId = this.externalId,
        bucket = this.bucket.path
    )
}

fun ChatEntity.toChatBucketInfo() = ChatBucketInfo(chatId = this.id!!, Bucket(this.bucket))