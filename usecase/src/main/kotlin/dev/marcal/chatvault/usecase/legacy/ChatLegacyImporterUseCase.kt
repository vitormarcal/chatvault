package dev.marcal.chatvault.usecase.legacy

import dev.marcal.chatvault.app_service.wpp_legacy_service.WppLegacyService
import dev.marcal.chatvault.app_service.dto.ChatDTO
import dev.marcal.chatvault.app_service.dto.MessageDTO
import dev.marcal.chatvault.app_service.dto.WppChatResponse
import dev.marcal.chatvault.service.legacy.ChatLegacyImporter
import dev.marcal.chatvault.service.ChatCreator
import dev.marcal.chatvault.service.MessageCreator
import dev.marcal.chatvault.in_out_boundary.input.NewAttachmentInput
import dev.marcal.chatvault.in_out_boundary.input.NewChatInput
import dev.marcal.chatvault.in_out_boundary.input.NewMessageInput
import dev.marcal.chatvault.in_out_boundary.input.NewMessagePayloadInput
import dev.marcal.chatvault.in_out_boundary.output.ChatBucketInfoOutput
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux

@Service
class ChatLegacyImporterUseCase(
    private val wppLegacyService: WppLegacyService,
    private val messageCreator: MessageCreator,
    private val chatCreator: ChatCreator
) : ChatLegacyImporter {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    override fun importToEventSource() {
        getAllChats()
            .map { chatDTO ->
                chatDTO to createChatIfNotExists(chatDTO)
            }
            .flatMap { (chat, bucketInfo) -> processMessagesInBatches(chat, bucketInfo) }
            .doOnNext { input ->
                messageCreator.execute(input)
            }
            .subscribe {
                logger.info("Imported: chatId=${it.chatId}, messages=${it.messages.size}")
            }


    }

    private fun createChatIfNotExists(it: ChatDTO) =
        chatCreator.executeIfNotExists(NewChatInput(name = it.name, externalId = it.id.toString()))

    private fun getAllChats() = wppLegacyService.getAllChats()
        .doOnNext {
            logger.info("Found ${it.count} chats")
        }.flatMapIterable { chatResponse -> chatResponse.data.sortedBy { it.id } }

    private fun findMessagesAndCreatePayloadInput(chat: ChatDTO, bucketInfo: ChatBucketInfoOutput, offset: Int, limit: Int) =
        wppLegacyService.getMessagesByChatId(chatId = chat.id, offset = offset, limit)
            .doOnNext { logger.info("Found ${it.data.size} messages") }
            .map { response -> toMessagePayloadInput(bucketInfo, response) }

    private fun toMessagePayloadInput(
        bucketInfo: ChatBucketInfoOutput,
        response: WppChatResponse<MessageDTO>
    ) = NewMessagePayloadInput(
        chatId = bucketInfo.chatId,
        eventSource = true,
        messages = response.data.map { message ->
            NewMessageInput(
                chatId = bucketInfo.chatId,
                externalId = message.id.toString(),
                authorName = message.author ?: "",
                createdAt = message.date,
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