package dev.marcal.chatvault.api.dto.mapper

import dev.marcal.chatvault.api.dto.input.NewAttachmentInput
import dev.marcal.chatvault.api.dto.input.NewMessageInput
import dev.marcal.chatvault.api.dto.output.AttachmentInfoOutput
import dev.marcal.chatvault.api.dto.output.ChatBucketInfoOutput
import dev.marcal.chatvault.api.dto.output.ChatLastMessageOutput
import dev.marcal.chatvault.api.dto.output.MessageOutput
import dev.marcal.chatvault.domain.model.AttachmentSummary
import dev.marcal.chatvault.domain.model.ChatBucketInfo
import dev.marcal.chatvault.domain.model.ChatLastMessage
import dev.marcal.chatvault.domain.model.Message

fun ChatBucketInfo.toOutput(): ChatBucketInfoOutput =
    ChatBucketInfoOutput(
        chatId = this.chatId,
        path = this.bucket.path,
    )

fun ChatLastMessage.toOutput(): ChatLastMessageOutput =
    ChatLastMessageOutput(
        chatId = this.chatId,
        chatName = this.chatName,
        authorName = this.author.name,
        authorType = this.author.type.name,
        content = this.content,
        msgCreatedAt = this.msgCreatedAt,
        msgCount = this.msgCount,
    )

fun Message.toOutput(): MessageOutput =
    MessageOutput(
        id = null,
        content = this.content.text,
        createdAt = this.createdAt,
        attachmentName = this.content.attachment?.name,
        authorType = this.author.type.name,
        author = this.author.name,
    )

fun MessageOutput.toNewMessageInput(chatId: Long): NewMessageInput =
    NewMessageInput(
        authorName = this.author ?: "",
        chatId = chatId,
        createdAt = this.createdAt,
        content = this.content,
        attachment = this.attachmentName?.let { NewAttachmentInput(name = it, content = "") },
    )

fun NewMessageInput.toMessageDomain(chatBucketInfo: ChatBucketInfo): Message =
    Message.create(
        chatBucketInfo = chatBucketInfo,
        authorName = this.authorName,
        content = this.content,
        createdAt = this.createdAt,
        externalId = this.externalId,
        attachmentName = this.attachment?.name,
    )

fun AttachmentSummary.toOutput(): AttachmentInfoOutput =
    AttachmentInfoOutput(
        id = this.id,
        name = this.name,
    )
