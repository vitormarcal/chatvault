package dev.marcal.chatvault.usecase

import dev.marcal.chatvault.app_service.bucket_service.BucketService
import dev.marcal.chatvault.in_out_boundary.input.NewMessagePayloadInput
import dev.marcal.chatvault.model.BucketFile
import dev.marcal.chatvault.repository.ChatRepository
import dev.marcal.chatvault.service.ChatFileImporter
import dev.marcal.chatvault.service.ChatMessageParser
import dev.marcal.chatvault.service.MessageCreator
import dev.marcal.chatvault.usecase.mapper.toNewMessageInput
import org.springframework.stereotype.Service
import java.io.BufferedInputStream
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.lang.IllegalStateException
import java.util.zip.ZipInputStream

@Service
class ChatFileImporterUseCase(
    private val chatMessageParser: ChatMessageParser,
    private val messageCreator: MessageCreator,
    private val bucketService: BucketService,
    private val chatRepository: ChatRepository
) : ChatFileImporter {

    private val possiblyWhatsappTalk = Regex(".*WhatsApp.*\\.txt$")
    override fun execute(chatId: Long, inputStream: InputStream, fileType: String) {
        when (fileType) {
            "zip" -> {
                iterateOverZip(chatId, inputStream)
            }

            "text" -> {
                createMessages(inputStream = inputStream, chatId = chatId)
            }

            else -> throw IllegalStateException("file type $fileType not supported")
        }


    }

    private fun iterateOverZip(chatId: Long, inputStream: InputStream) {
        val chatBucketInfo =
            chatRepository.findChatBucketInfoByChatId(chatId = chatId) ?: throw RuntimeException("chatId not found")
        val bucket = chatBucketInfo.bucket.withPath("/")

        val zipInputStream = ZipInputStream(BufferedInputStream(inputStream))

        var entry = zipInputStream.nextEntry
        while (entry != null) {
            val fileName = entry.name

            val buffer = ByteArray(1024)
            zipInputStream.read(buffer)

            bucketService.save(BucketFile(bytes = buffer, fileName = fileName, address = bucket))

            if (possiblyWhatsappTalk.find(fileName) != null) {
                execute(chatId = chatId, inputStream = ByteArrayInputStream(buffer), fileType = "text")
            }

            entry = zipInputStream.nextEntry
        }
        zipInputStream.close()
    }

    private fun createMessages(inputStream: InputStream, chatId: Long) {
        val messages = chatMessageParser.parseAndTransform(inputStream) { messageOutput ->
            messageOutput.toNewMessageInput(chatId = chatId)
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