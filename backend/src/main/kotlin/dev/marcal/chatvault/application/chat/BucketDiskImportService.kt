package dev.marcal.chatvault.application.chat

import dev.marcal.chatvault.api.dto.input.FileTypeInputEnum
import dev.marcal.chatvault.api.dto.input.PendingChatFile
import dev.marcal.chatvault.domain.bucket.BucketService
import dev.marcal.chatvault.domain.model.Bucket
import dev.marcal.chatvault.domain.model.BucketFile
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service

@Service
class BucketDiskImportService(
    private val bucketService: BucketService,
    private val chatFileImporter: ChatImportService,
    private val chatCreator: ChatCreatorService,
) {
    fun execute(chatName: String? = null) {
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

    fun saveToImportDir(pendingList: List<PendingChatFile>) {
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
        val chatBucketInfo = chatCreator.findOrCreateByName(chatName)
        return chatBucketInfo.chatId to resource
    }
}
