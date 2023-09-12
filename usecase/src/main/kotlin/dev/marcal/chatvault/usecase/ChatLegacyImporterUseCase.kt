package dev.marcal.chatvault.usecase

import dev.marcal.chatvault.app_service.WppLegacyService
import dev.marcal.chatvault.app_service.dto.ChatDTO
import dev.marcal.chatvault.app_service.dto.MessageDTO
import dev.marcal.chatvault.app_service.dto.WppChatResponse
import dev.marcal.chatvault.model.MessagePayload
import dev.marcal.chatvault.service.ChatLegacyImporter
import dev.marcal.chatvault.service.NewChat
import dev.marcal.chatvault.service.NewMessage
import dev.marcal.chatvault.service.input.NewAttachmentInput
import dev.marcal.chatvault.service.input.NewChatInput
import dev.marcal.chatvault.service.input.NewMessageInput
import dev.marcal.chatvault.service.input.NewMessagePayloadInput
import dev.marcal.chatvault.service.output.ChatBucketInfoOutput
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import java.time.LocalDateTime

@Service
class ChatLegacyImporterUseCase(
    private val wppLegacyService: WppLegacyService,
    private val newMessage: NewMessage,
    private val newChat: NewChat
) : ChatLegacyImporter {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    override fun execute() {
        getAllChats()
            .map { chatDTO ->
                chatDTO to createChatIfNotExists(chatDTO)
            }
            .flatMap { (chat, bucketInfo) -> processMessagesInBatches(chat, bucketInfo) }
            .doOnNext { input ->
                newMessage.execute(input)
            }
            .subscribe {
                logger.info("Imported: chatId=${it.chatId}, messages=${it.messages.size}")
            }


    }

    private fun createChatIfNotExists(it: ChatDTO) =
        newChat.executeIfNotExists(NewChatInput(name = it.name, externalId = it.id.toString()))

    private fun getAllChats() = wppLegacyService.getAllChats()
        .doOnNext {
            logger.info("Found ${it.count} chats")
        }.flatMapIterable { chatResponse -> chatResponse.data }

    private fun findMessagesAndCreatePayloadInput(chat: ChatDTO, bucketInfo: ChatBucketInfoOutput, offset: Int, limit: Int) =
        wppLegacyService.getMessagesByChatId(chatId = chat.id, offset = offset, limit)
            .doOnNext { logger.info("Found ${it.data.size} messages") }
            .map { response -> toMessagePayloadInput(bucketInfo, response) }

    private fun toMessagePayloadInput(
        bucketInfo: ChatBucketInfoOutput,
        response: WppChatResponse<MessageDTO>
    ) = NewMessagePayloadInput(
        chatId = bucketInfo.chatId,
        messages = response.data.map { message ->
            NewMessageInput(
                chatId = bucketInfo.chatId,
                externalId = message.id.toString(),
                authorName = message.author ?: "",
                createdAt = LocalDateTime.now(),
                content = message.content,
                attachment = message.attachmentName?.let { NewAttachmentInput(name = it, content = "") }
            )
        }
    )

    private fun processMessagesInBatches(chat: ChatDTO, bucketInfo: ChatBucketInfoOutput, pageSize: Int = 1000): Flux<NewMessagePayloadInput> {
        val totalMessages = chat.messagesCount
        val totalPages = (totalMessages + pageSize - 1) / pageSize
        return Flux.create { emitter ->

            fun processPage(currentPage: Int) {
                findMessagesAndCreatePayloadInput(chat = chat, bucketInfo = bucketInfo, offset = currentPage * pageSize, limit = pageSize)
                    .doOnNext {
                        logger.info("Fetched chatId=${chat.id} $currentPage of $totalPages pages total $totalMessages")
                    }
                    .subscribe(
                        { input -> emitter.next(input) },
                        { error -> emitter.error(error) },
                        {
                            if (currentPage == totalPages - 1) {
                                emitter.complete()
                            } else {
                                processPage(currentPage + 1)
                            }
                        }
                    )
            }

            processPage(0)
        }
    }
}