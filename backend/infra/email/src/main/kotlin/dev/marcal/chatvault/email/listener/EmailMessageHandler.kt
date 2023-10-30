package dev.marcal.chatvault.email.listener

import dev.marcal.chatvault.in_out_boundary.input.FileTypeInputEnum
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
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@Component
@ConditionalOnProperty(prefix = "app.email", name = ["enabled"], havingValue = "true", matchIfMissing = true)
class EmailMessageHandler(
    private val chatFileImporter: ChatFileImporter,
    @Value("\${app.email.subject-starts-with}") private val subjectStartsWithList: List<String>,
    @Value("\${app.email.debug}") private val emailDebug: Boolean
) {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    @ServiceActivator(inputChannel = "imapChannel")
    fun handleMessage(message: Message<MimeMessage>) {
        val mimeMessage = message.payload.also {
            if (emailDebug) logger.info("Starting to process new email message ${it.subject}")
        }

        useFolder(mimeMessage) {
            if (mimeMessage.isMimeType("multipart/*")) {
                val multipart = mimeMessage.content as Multipart

                val (fileType, inputStream) = getInputStream(multipart)
                chatFileImporter.execute(
                    chatName = getChatNameOrNull(mimeMessage),
                    inputStream,
                    fileType = fileType
                )
            }
        }
    }

    fun useFolder(mimeMessage: MimeMessage, doIt: (MimeMessage) -> Unit) {
        val folder: Folder = mimeMessage.folder
        try {
            if (!folder.isOpen) { folder.open(Folder.READ_ONLY) }
            doIt(mimeMessage)
        } catch (e: Exception) {
            logger.error("Fail to handle message ${mimeMessage.subject}", e)
            throw e
        } finally {
            folder.close(false)
        }

    }

    private fun getInputStream(multipart: Multipart): Pair<FileTypeInputEnum, InputStream> {
        val indexStart = 1 // because 0 is the body text (ignored)
        return if (multipart.count == 2) {
            // only one file (body email text and attachment), body text is ignored
            val bodyPart = multipart.getBodyPart(indexStart)
            val attachmentInputStream: InputStream = bodyPart.inputStream
            if (bodyPart.fileName.endsWith(".zip", ignoreCase = true)) {
                FileTypeInputEnum.ZIP to attachmentInputStream
            } else {
                FileTypeInputEnum.TEXT to attachmentInputStream
            }
        } else {
            //many files
            val outputStream = ByteArrayOutputStream()
            val zipOutputStream = ZipOutputStream(outputStream)
            for (i in indexStart until multipart.count) {
                val bodyPart = multipart.getBodyPart(i)
                val inputStream: InputStream = bodyPart.inputStream

                zipOutputStream.putNextEntry(ZipEntry(bodyPart.fileName))
                val buffer = ByteArray(1024)
                var len: Int
                while (inputStream.read(buffer).also { len = it } > 0) {
                    zipOutputStream.write(buffer, 0, len)
                }
                zipOutputStream.closeEntry()
                inputStream.close()
            }
            zipOutputStream.close()
            FileTypeInputEnum.ZIP to ByteArrayInputStream(outputStream.toByteArray())
        }
    }

    private fun getChatNameOrNull(mimeMessage: MimeMessage): String? {
        val subjectStartsWith = subjectStartsWithList.first { mimeMessage.subject.startsWith(it, ignoreCase = true) }
        val chatName = Regex("(?i)($subjectStartsWith)(.*)").find(mimeMessage.subject)?.let {
            it.groupValues[2]
        }
        return chatName?.trim()
    }
}