package dev.marcal.chatvault.persistence.dto

import dev.marcal.chatvault.model.ChatLastMessage
import dev.marcal.chatvault.persistence.entity.toAuthorDomain

fun ChatMessagePairDTO.toChatLastMessage(): ChatLastMessage {
    return ChatLastMessage(
        chatId = requireNotNull(this.chat.id),
        chatName = this.chat.name,
        author = this.message.toAuthorDomain(),
        content = this.message.content,
        msgCreatedAt = this.message.createdAt,
        msgCount = this.amountOfMessages
    )
}