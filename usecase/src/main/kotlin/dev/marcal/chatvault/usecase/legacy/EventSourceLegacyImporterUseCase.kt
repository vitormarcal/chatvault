package dev.marcal.chatvault.usecase.legacy

import dev.marcal.chatvault.model.MessagePayload
import dev.marcal.chatvault.repository.ChatRepository
import dev.marcal.chatvault.service.legacy.EventSourceLegacyImporter
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class EventSourceLegacyImporterUseCase(
    private val chatRepository: ChatRepository
) : EventSourceLegacyImporter {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    override fun importMessagesFromEventSource() {


        chatRepository.findAllEventSourceChatId()
            .parallelStream()
            .forEach { chatId ->
                do {
                    val page = chatRepository.findLegacyToImport(chatId = chatId, page = 1, size = 100)
                    logger.info("chatId: $chatId page ${page.totalPages}, totalItems: ${page.totalItems}")
                    importMessage(
                        MessagePayload(
                            chatId = chatId,
                            messages = page.data
                        )
                    )

                } while (page.totalPages > 1)
            }
        logger.info("finish")


    }

    private fun importMessage(messagePayload: MessagePayload) {
        chatRepository.saveLegacyMessage(
            messagePayload
        )
    }
}