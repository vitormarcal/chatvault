package dev.marcal.chatvault.usecase

import dev.marcal.chatvault.app_service.bucket_service.BucketService
import dev.marcal.chatvault.in_out_boundary.output.MessageOutput
import dev.marcal.chatvault.in_out_boundary.output.exceptions.ChatNotFoundException
import dev.marcal.chatvault.model.AuthorType
import dev.marcal.chatvault.model.BucketFile
import dev.marcal.chatvault.repository.ChatRepository
import dev.marcal.chatvault.service.ChatFileExporter
import dev.marcal.chatvault.service.MessageFinderByChatId
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import java.time.format.DateTimeFormatter


@Service
class ChatFileExporterUseCase(
    private val bucketService: BucketService,
    private val chatRepository: ChatRepository,
    private val messageFinderByChatId: MessageFinderByChatId
) : ChatFileExporter {

    private val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

    override fun execute(chatId: Long): Resource {
        val chatBucketInfo =
            chatRepository.findChatBucketInfoByChatId(chatId) ?: throw ChatNotFoundException("The export failed. Chat with id $chatId was not found.")

        messageFinderByChatId.execute(chatId).map {
            parseToLineText(it)
        }.also { sequence ->
            bucketService.saveTextToBucket(
                bucketFile = BucketFile(
                    fileName = "_WhatsApp talk.txt",
                    address = chatBucketInfo.bucket
                ), sequence
            )
        }

        return bucketService.loadBucketAsZip(chatBucketInfo.bucket.path)

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