package dev.marcal.chatvault.infrastructure.persistence.dto
import dev.marcal.chatvault.infrastructure.persistence.entity.ChatEntity
import dev.marcal.chatvault.infrastructure.persistence.entity.MessageEntity

data class ChatMessagePairDTO(
    val chat: ChatEntity,
    val message: MessageEntity,
    val amountOfMessages: Long,
)
