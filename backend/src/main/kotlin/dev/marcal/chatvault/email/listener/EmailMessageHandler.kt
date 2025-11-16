package dev.marcal.chatvault.email.listener

import dev.marcal.chatvault.ioboundary.input.PendingChatFile
import dev.marcal.chatvault.service.BucketDiskImporter
import dev.marcal.chatvault.service.ChatFileImporter
import jakarta.mail.Folder
import jakarta.mail.Multipart
import jakarta.mail.internet.MimeMessage
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.integration.annotation.ServiceActivator
import org.springframework.messaging.Message
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
@ConditionalOnProperty(prefix = "chatvault.email", name = ["enabled"], havingValue = "true", matchIfMissing = true)
class EmailMessageHandler(
    private val chatFileImporter: ChatFileImporter,
    @Value("\${chatvault.email.subject-starts-with}") private val subjectStartsWithList: List<String>,
    @Value("\${chatvault.email.debug}") private val emailDebug: Boolean,
    private val bucketDiskImporter: BucketDiskImporter,
) {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    @ServiceActivator(inputChannel = "imapChannel")
    fun handleMessage(message: Message<MimeMessage>) {
        val mimeMessage =
            message.payload.also {
                if (emailDebug) logger.info("Starting to process new email message ${it.subject}")
            }

        useFolder(mimeMessage) {
            if (mimeMessage.isMimeType("multipart/*")) {
                val multipart = mimeMessage.content as Multipart

                val chatName = getChatName(mimeMessage)
                val pendingList = getInputStream(chatName, multipart)
                bucketDiskImporter.apply {
                    this.saveToImportDir(pendingList)
                    this.execute(chatName)
                }
            }
        }
    }

    fun useFolder(
        mimeMessage: MimeMessage,
        doIt: (MimeMessage) -> Unit,
    ) {
        val folder: Folder = mimeMessage.folder
        try {
            if (!folder.isOpen) {
                folder.open(Folder.READ_ONLY)
            }
            doIt(mimeMessage)
        } catch (e: Exception) {
            logger.error("Fail to handle message ${mimeMessage.subject}", e)
            throw e
        } finally {
            folder.close(false)
        }
    }

    private fun getInputStream(
        chatName: String,
        multipart: Multipart,
    ): List<PendingChatFile> {
        val indexStart = 1 // because 0 is the body text (ignored)
        return if (multipart.count == 2) {
            // only one file (body email text and attachment), body text is ignored
            val bodyPart = multipart.getBodyPart(indexStart)
            listOf(
                PendingChatFile(
                    stream = bodyPart.inputStream,
                    fileName = bodyPart.fileName,
                    chatName = chatName,
                ),
            )
        } else {
            // many files
            (indexStart until multipart.count)
                .asSequence()
                .map { multipart.getBodyPart(it) }
                .map {
                    PendingChatFile(
                        stream = it.inputStream,
                        fileName = it.fileName,
                        chatName = chatName,
                    )
                }.toList()
        }
    }

    private fun getChatName(mimeMessage: MimeMessage): String =
        getChatNameOrNull(mimeMessage) ?: ("todo ${mimeMessage.subject} ${LocalDateTime.now()}")

    private fun getChatNameOrNull(mimeMessage: MimeMessage): String? {
        val subjectStartsWith = subjectStartsWithList.first { mimeMessage.subject.startsWith(it, ignoreCase = true) }
        val chatName =
            Regex("(?i)($subjectStartsWith)(.*)").find(mimeMessage.subject)?.let {
                it.groupValues[2]
            }
        return chatName?.trim()
    }
}
