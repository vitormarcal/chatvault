package dev.marcal.chatvault.usecase.mapper

import dev.marcal.chatvault.model.ChatBucketInfo
import dev.marcal.chatvault.model.ChatLastMessage
import dev.marcal.chatvault.in_out_boundary.output.ChatBucketInfoOutput
import dev.marcal.chatvault.in_out_boundary.output.ChatLastMessageOutput
import dev.marcal.chatvault.in_out_boundary.output.MessageOutput
import dev.marcal.chatvault.model.Message

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

fun Message.toOutput(): MessageOutput {
    return MessageOutput(
        id = null,
        content = this.content.text,
        createdAt = this.createdAt,
        attachmentName = this.content.attachment?.name,
        authorType = this.author.type.name,
        author = this.author.name
    )
}