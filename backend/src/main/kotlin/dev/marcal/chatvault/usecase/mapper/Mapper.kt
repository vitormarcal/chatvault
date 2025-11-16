package dev.marcal.chatvault.usecase.mapper

import dev.marcal.chatvault.domain.model.Attachment
import dev.marcal.chatvault.domain.model.Author
import dev.marcal.chatvault.domain.model.AuthorType
import dev.marcal.chatvault.domain.model.ChatBucketInfo
import dev.marcal.chatvault.domain.model.ChatLastMessage
import dev.marcal.chatvault.domain.model.Content
import dev.marcal.chatvault.domain.model.Message
import dev.marcal.chatvault.ioboundary.input.NewAttachmentInput
import dev.marcal.chatvault.ioboundary.input.NewMessageInput
import dev.marcal.chatvault.ioboundary.output.ChatBucketInfoOutput
import dev.marcal.chatvault.ioboundary.output.ChatLastMessageOutput
import dev.marcal.chatvault.ioboundary.output.MessageOutput
import java.time.LocalDateTime

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
    Message(
        author =
            Author(
                name = this.authorName,
                type = if (this.authorName.isEmpty()) AuthorType.SYSTEM else AuthorType.USER,
            ),
        createdAt = this.createdAt ?: LocalDateTime.now(),
        externalId = this.externalId,
        content =
            Content(
                text = this.content,
                attachment =
                    this.attachment?.let {
                        Attachment(
                            name = it.name,
                            bucket = chatBucketInfo.bucket.withPath("/"),
                        )
                    },
            ),
    )
