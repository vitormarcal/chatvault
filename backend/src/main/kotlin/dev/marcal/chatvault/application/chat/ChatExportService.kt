package dev.marcal.chatvault.application.chat

import dev.marcal.chatvault.api.dto.output.MessageOutput
import dev.marcal.chatvault.api.web.exception.ChatNotFoundException
import dev.marcal.chatvault.application.message.MessageFinderService
import dev.marcal.chatvault.domain.bucket.BucketService
import dev.marcal.chatvault.domain.model.AuthorType
import dev.marcal.chatvault.domain.model.Bucket
import dev.marcal.chatvault.domain.model.BucketFile
import dev.marcal.chatvault.domain.repository.ChatRepository
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import java.time.format.DateTimeFormatter

@Service
class ChatExportService(
    private val bucketService: BucketService,
    private val chatRepository: ChatRepository,
    private val messageFinderByChatId: MessageFinderService,
) {
    private val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

    fun execute(chatId: Long): Resource {
        val bucket = findMessagesAndUpdateBucket(chatId)

        return bucketService.loadBucketAsZip(bucket.path)
    }

    fun executeAll(): Resource {
        chatRepository
            .findAllChatsWithLastMessage()
            .forEach { findMessagesAndUpdateBucket(it.chatId) }
        return bucketService.loadBucketListAsZip()
    }

    fun findMessagesAndUpdateBucket(chatId: Long): Bucket {
        val chatBucketInfo =
            chatRepository.findChatBucketInfoByChatId(chatId)
                ?: throw ChatNotFoundException("The export failed. Chat with id $chatId was not found.")

        messageFinderByChatId
            .execute(chatId)
            .map {
                parseToLineText(it)
            }.also { sequence ->
                bucketService.saveTextToBucket(
                    bucketFile =
                        BucketFile(
                            fileName = "_WhatsApp talk.txt",
                            address = chatBucketInfo.bucket,
                        ),
                    sequence,
                )
            }
        return chatBucketInfo.bucket
    }

    private fun parseToLineText(it: MessageOutput): String {
        val builder = StringBuilder(it.content.length + 100)
        builder.append(formatter.format(it.createdAt))
        builder.append(" - ")
        if (it.authorType == AuthorType.USER.name) {
            builder.append("${it.author}: ")
        }
        builder.append(it.content)
        return builder.toString()
    }
}
