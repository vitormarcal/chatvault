package dev.marcal.chatvault.persistence.dto

import dev.marcal.chatvault.model.Author
import dev.marcal.chatvault.model.AuthorType
import dev.marcal.chatvault.model.ChatLastMessage

fun ChatMessagePairDTO.toChatLastMessage(): ChatLastMessage {
    return ChatLastMessage(
        chatId = requireNotNull(this.chat.id),
        chatName = this.chat.name,
        author = Author(name = this.message.author, type = AuthorType.valueOf(this.message.authorType)),
        content = this.message.content,
        msgCreatedAt = this.message.createdAt,
    )
}