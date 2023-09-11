package dev.marcal.chatvault.usecase.mapper

import dev.marcal.chatvault.model.ChatBucketInfo
import dev.marcal.chatvault.service.output.ChatBucketInfoOutput

fun ChatBucketInfo.toOutput(): ChatBucketInfoOutput {
    return ChatBucketInfoOutput(
        chatId = this.chatId,
        path = this.bucket.path
    )
}