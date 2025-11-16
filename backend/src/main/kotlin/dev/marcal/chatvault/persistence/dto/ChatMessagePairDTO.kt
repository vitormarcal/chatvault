package dev.marcal.chatvault.persistence.dto
import dev.marcal.chatvault.persistence.entity.ChatEntity
import dev.marcal.chatvault.persistence.entity.MessageEntity

data class ChatMessagePairDTO(
    val chat: ChatEntity,
    val message: MessageEntity,
    val amountOfMessages: Long,
)
