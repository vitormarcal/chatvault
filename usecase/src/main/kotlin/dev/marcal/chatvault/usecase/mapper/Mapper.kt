package dev.marcal.chatvault.usecase.mapper

import dev.marcal.chatvault.model.ChatBucketInfo
import dev.marcal.chatvault.model.ChatLastMessage
import dev.marcal.chatvault.service.output.ChatBucketInfoOutput
import dev.marcal.chatvault.service.output.ChatLastMessageOutput

fun ChatBucketInfo.toOutput(): ChatBucketInfoOutput {
    return ChatBucketInfoOutput(
        chatId = this.chatId,
        path = this.bucket.path
    )
}

fun ChatLastMessage.toOutput(): ChatLastMessageOutput {
    return ChatLastMessageOutput(
        chatId = this.chatId,
        chatName = this.chatName,
        authorName = this.author.name,
        authorType = this.author.type.name,
        content = this.content,
        msgCreatedAt = this.msgCreatedAt
    )
}