package dev.marcal.chatvault.persistence.entity

import dev.marcal.chatvault.model.Chat
import dev.marcal.chatvault.model.ChatPayload
import dev.marcal.chatvault.model.MessagePayload


fun MessagePayload.toMessagesEntity(): List<MessageEntity> {
    return this.messages.map {
        MessageEntity(
            author = it.author.name,
            authorType = it.author.type.name,
            content = it.content.text,
            attachmentPath = it.content.attachment?.bucket?.path,
            chatId = this.chatId,
            createdAt = it.createdAt
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