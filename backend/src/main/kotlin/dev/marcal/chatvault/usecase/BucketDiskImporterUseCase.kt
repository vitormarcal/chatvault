package dev.marcal.chatvault.usecase

import dev.marcal.chatvault.domain.bucketservice.BucketService
import dev.marcal.chatvault.domain.model.Bucket
import dev.marcal.chatvault.domain.model.BucketFile
import dev.marcal.chatvault.domain.model.ChatBucketInfo
import dev.marcal.chatvault.domain.repository.ChatRepository
import dev.marcal.chatvault.ioboundary.input.FileTypeInputEnum
import dev.marcal.chatvault.ioboundary.input.NewChatInput
import dev.marcal.chatvault.ioboundary.input.PendingChatFile
import dev.marcal.chatvault.service.BucketDiskImporter
import dev.marcal.chatvault.service.ChatCreator
import dev.marcal.chatvault.service.ChatFileImporter
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service

@Service
class BucketDiskImporterUseCase(
    private val bucketService: BucketService,
    private val chatRepository: ChatRepository,
    private val chatFileImporter: ChatFileImporter,
    private val chatCreator: ChatCreator,
) : BucketDiskImporter {
    override fun execute(chatName: String?) {
        bucketService
            .zipPendingImports(chatName)
            .map { identifyChat(it) }
            .forEach { (chatId, resource) ->
                chatFileImporter.execute(
                    chatId = chatId,
                    fileType = FileTypeInputEnum.ZIP,
                    inputStream = resource.inputStream,
                )
                bucketService.deleteZipImported(resource.filename!!)
            }
    }

    override fun saveToImportDir(pendingList: List<PendingChatFile>) {
        pendingList
            .map {
                BucketFile(
                    stream = it.stream,
                    fileName = it.fileName,
                    address = Bucket(it.chatName),
                )
            }.forEach {
                bucketService.saveToImportDir(it)
            }
    }

    private fun identifyChat(resource: Resource): Pair<Long, Resource> {
        val chatName = requireNotNull(resource.filename).removeSuffix(".zip")
        val chatBucketInfo = findOrCreateChatIfNotExists(chatName)
        return chatBucketInfo.chatId to resource
    }

    private fun findOrCreateChatIfNotExists(chatName: String): ChatBucketInfo {
        val chatBucketInfo =
            chatRepository.findChatBucketInfoByChatName(chatName) ?: run {
                chatCreator.executeIfNotExists(NewChatInput(name = chatName))
                chatRepository.findChatBucketInfoByChatName(chatName)
            }

        return requireNotNull(chatBucketInfo) { "error when finding and trying to create chat not existent $chatName" }
    }
}
