package dev.marcal.chatvault.usecase.legacy

import dev.marcal.chatvault.app_service.WppLegacyService
import dev.marcal.chatvault.model.Message
import dev.marcal.chatvault.model.Page
import dev.marcal.chatvault.repository.ChatRepository
import dev.marcal.chatvault.service.legacy.AttachmentLegacyImporter
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.MonoSink
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


@Service
class AttachmentLegacyImporterUseCase(
    private val chatRepository: ChatRepository,
    private val wppLegacyService: WppLegacyService
) : AttachmentLegacyImporter {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    override fun execute() {

        chatRepository.findAllEventSourceChatId()
            .parallelStream()
            .forEach { chatId ->
                do {
                    val page = findPage(chatId)
                    Flux.fromIterable(page.data)
                        .flatMap { downloadAndSaveFile(it) }
                        .doOnNext { logger.info("finish to $it") }
                        .then()
                        .block()
                    logger.info("finish ${page.page} of ${page.totalPages}")

                } while (page.totalPages > 1)
            }

    }

    private fun findPage(chatId: Long): Page<Message> {
        val page = chatRepository.findAttachmentLegacyToImport(chatId = chatId, page = 1, size = 100)
        logger.info("chatId: $chatId page ${page.totalPages}, totalItems: ${page.totalItems}")
        return page
    }

    private fun downloadAndSaveFile(message: Message): Mono<Message> {
        return wppLegacyService.getAttachmentsByMessageId(requireNotNull(message.externalId))
            .flatMap { bytes -> writeFile(bytes, message) }
            .doOnNext { chatRepository.setLegacyAttachmentImported(it.externalId.toString()) }
            .doOnError { logger.error("fail to get $message", it) }
            .onErrorComplete()


    }

    private fun writeFile(bytes: ByteArray, message: Message): Mono<Message> {
        File("/opt/chatvault/" + message.content.attachment!!.bucket.path).takeIf { !it.exists() }?.also { it.mkdirs() }
        val filePath = "/opt/chatvault/" + message.content.attachment!!.path()
        return Mono.create { sink: MonoSink<Message> ->
            try {
                FileOutputStream(File(filePath)).use { fos ->
                    fos.write(bytes)
                    fos.flush()
                    sink.success(message)
                }
            } catch (e: IOException) {
                sink.error(e)
            }
        }
    }

}