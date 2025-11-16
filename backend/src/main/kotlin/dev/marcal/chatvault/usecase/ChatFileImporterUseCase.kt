package dev.marcal.chatvault.usecase

import dev.marcal.chatvault.domain.bucketservice.BucketService
import dev.marcal.chatvault.domain.model.BucketFile
import dev.marcal.chatvault.domain.model.ChatNamePatternMatcher
import dev.marcal.chatvault.domain.repository.ChatRepository
import dev.marcal.chatvault.ioboundary.input.FileTypeInputEnum
import dev.marcal.chatvault.ioboundary.input.NewChatInput
import dev.marcal.chatvault.ioboundary.input.NewMessagePayloadInput
import dev.marcal.chatvault.ioboundary.output.exceptions.ChatImporterException
import dev.marcal.chatvault.service.ChatCreator
import dev.marcal.chatvault.service.ChatFileImporter
import dev.marcal.chatvault.service.ChatMessageParser
import dev.marcal.chatvault.service.MessageCreator
import dev.marcal.chatvault.usecase.mapper.toNewMessageInput
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.BufferedInputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.time.LocalDateTime
import java.util.zip.ZipInputStream

@Service
class ChatFileImporterUseCase(
    private val chatMessageParser: ChatMessageParser,
    private val messageCreator: MessageCreator,
    private val bucketService: BucketService,
    private val chatRepository: ChatRepository,
    private val chatCreator: ChatCreator,
) : ChatFileImporter {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    override fun execute(
        chatId: Long,
        inputStream: InputStream,
        fileType: FileTypeInputEnum,
    ) {
        when (fileType) {
            FileTypeInputEnum.ZIP -> {
                iterateOverZip(chatId, inputStream)
            }

            FileTypeInputEnum.TEXT -> {
                createMessages(inputStream = inputStream, chatId = chatId)
            }

            else -> throw IllegalStateException("file type $fileType not supported")
        }
    }

    override fun execute(
        chatName: String?,
        inputStream: InputStream,
        fileType: FileTypeInputEnum,
    ) {
        val chatId =
            chatName?.let { chatRepository.findChatBucketInfoByChatName(it)?.chatId } ?: createTodoChat(chatName)
        execute(chatId, inputStream, fileType)
    }

    private fun createTodoChat(chatName: String?): Long {
        val tempChatName = chatName?.takeIf { it.isNotEmpty() } ?: "todo imported at ${LocalDateTime.now()}"
        chatCreator.executeIfNotExists(NewChatInput(name = tempChatName))
        return requireNotNull(chatRepository.findChatBucketInfoByChatName(tempChatName)?.chatId) {
            "temp chat creation fails: chatName: $tempChatName"
        }
    }

    private fun iterateOverZip(
        chatId: Long,
        inputStream: InputStream,
    ) {
        val chatBucketInfo =
            chatRepository.findChatBucketInfoByChatId(chatId = chatId)
                ?: throw ChatImporterException("Chat id $chatId was not found. File import failed.")
        val bucket = chatBucketInfo.bucket.withPath("/")

        val zipInputStream = ZipInputStream(BufferedInputStream(inputStream))

        var entry = zipInputStream.nextEntry
        while (entry != null) {
            val fileName = entry.name

            val byteArray = bytes(zipInputStream)
            bucketService.save(BucketFile(bytes = byteArray, fileName = fileName, address = bucket))

            if (ChatNamePatternMatcher.matches(fileName)) {
                execute(
                    chatId = chatId,
                    inputStream = ByteArrayInputStream(byteArray),
                    fileType = FileTypeInputEnum.TEXT,
                )
            }

            entry = zipInputStream.nextEntry
        }
        zipInputStream.close()
    }

    private fun bytes(zipInputStream: ZipInputStream): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()
        val buffer = ByteArray(1024)
        var len: Int

        while (zipInputStream.read(buffer).also { len = it } > 0) {
            byteArrayOutputStream.write(buffer, 0, len)
        }

        return byteArrayOutputStream.toByteArray()
    }

    private fun createMessages(
        inputStream: InputStream,
        chatId: Long,
    ) {
        val messages =
            chatMessageParser.parseAndTransform(inputStream) { messageOutput ->
                messageOutput.toNewMessageInput(chatId = chatId)
            }

        val count = chatRepository.countChatMessages(chatId)

        messageCreator.execute(
            NewMessagePayloadInput(
                chatId = chatId,
                eventSource = false,
                messages = messages,
            ),
        )

        val countUpdated = chatRepository.countChatMessages(chatId)

        val newMessages = countUpdated - count

        logger.info("imported $newMessages of ${messages.size} messages  to chatId=$chatId")
    }
}
