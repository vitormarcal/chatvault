package dev.marcal.chatvault.usecase

import dev.marcal.chatvault.in_out_boundary.input.NewAttachmentInput
import dev.marcal.chatvault.in_out_boundary.input.NewMessageInput
import dev.marcal.chatvault.in_out_boundary.input.NewMessagePayloadInput
import dev.marcal.chatvault.service.ChatFileImporter
import dev.marcal.chatvault.service.ChatMessageParser
import dev.marcal.chatvault.service.MessageCreator
import org.springframework.stereotype.Service
import java.io.InputStream

@Service
class ChatFileImporterUseCase(
    private val chatMessageParser: ChatMessageParser,
    private val messageCreator: MessageCreator
) : ChatFileImporter {
    override fun execute(chatId: Long, inputStream: InputStream) {
        val messages = chatMessageParser.parseAndTransform(inputStream) { messageOutput ->
            NewMessageInput(
                authorName = messageOutput.author ?: "",
                chatId = chatId,
                createdAt = messageOutput.createdAt,
                content = messageOutput.content,
                attachment = messageOutput.attachmentName?.let { NewAttachmentInput(name = it, content = "") }
            )
        }

        messageCreator.execute(
            NewMessagePayloadInput(
                chatId = chatId,
                eventSource = false,
                messages = messages
            )
        )
    }
}