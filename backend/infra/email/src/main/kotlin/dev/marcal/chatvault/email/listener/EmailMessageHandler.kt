package dev.marcal.chatvault.email.listener

import dev.marcal.chatvault.service.ChatFileImporter
import jakarta.mail.Folder
import jakarta.mail.Multipart
import jakarta.mail.Part
import jakarta.mail.internet.MimeMessage
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.integration.annotation.ServiceActivator
import org.springframework.messaging.Message
import org.springframework.stereotype.Component
import java.io.InputStream

@Component
@ConditionalOnProperty(prefix = "app.email", name = ["enabled"], havingValue = "true", matchIfMissing = true)
class EmailMessageHandler(
    private val chatFileImporter: ChatFileImporter,
    @Value("\${app.email.subject-starts-with}") private val subjectStartsWithList: List<String>
) {

    @ServiceActivator(inputChannel = "imapChannel")
    fun handleMessage(message: Message<MimeMessage>) {
        // Aqui você pode implementar a lógica para processar a mensagem recebida.
        val mimeMessage = message.payload


        if (mimeMessage.isMimeType("multipart/*")) {
            val multipart = mimeMessage.content as Multipart

            // Itere pelos elementos no Multipart.
            for (i in 0 until multipart.count) {
                val bodyPart = multipart.getBodyPart(i)

                // Verifique se este é um anexo.
                if (Part.ATTACHMENT.equals(bodyPart.disposition, ignoreCase = true)) {
                    // Verifique se o nome do anexo é o que você procura (neste caso, um arquivo ZIP).
                    if (bodyPart.fileName.endsWith(".zip", ignoreCase = true)) {
                        // Obtenha o InputStream do anexo ZIP.
                        val folder: Folder = mimeMessage.folder
                        if (!folder.isOpen) {
                            folder.open(Folder.HOLDS_MESSAGES) // Ou o modo apropriado para a sua situação
                        }
                        val attachmentInputStream: InputStream = bodyPart.inputStream
                        // Certifique-se de fechar a pasta após terminar de acessar os anexos.


                        chatFileImporter.execute(
                            chatName = getChatNameOrNull(mimeMessage),
                            attachmentInputStream,
                            fileType = "zip"
                        )
                        folder.close(false)
                    }
                }
            }
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