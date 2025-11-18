package dev.marcal.chatvault.infrastructure.persistence.dto

import dev.marcal.chatvault.domain.model.ChatLastMessage
import dev.marcal.chatvault.infrastructure.persistence.entity.toAuthorDomain

fun ChatMessagePairDTO.toChatLastMessage(): ChatLastMessage =
    ChatLastMessage(
        chatId = requireNotNull(this.chat.id),
        chatName = this.chat.name,
        author = this.message.toAuthorDomain(),
        content = this.message.content,
        msgCreatedAt = this.message.createdAt,
        msgCount = this.amountOfMessages,
    )
