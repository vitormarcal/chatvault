package dev.marcal.chatvault.usecase

import dev.marcal.chatvault.app_service.bucket_service.BucketService
import dev.marcal.chatvault.in_out_boundary.input.NewChatInput
import dev.marcal.chatvault.model.ChatBucketInfo
import dev.marcal.chatvault.repository.ChatRepository
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
    private val chatCreator: ChatCreator
) : BucketDiskImporter {

    override fun execute() {
        bucketService.zipPendingImports()
            .map { identifyChat(it) }
            .forEach { (chatId, resource) ->
                chatFileImporter.execute(chatId = chatId, fileType = "zip", inputStream = resource.inputStream)
                bucketService.deleteZipImported(resource.filename!!)
            }
    }

    private fun identifyChat(resource: Resource): Pair<Long, Resource> {
        val chatName = requireNotNull(resource.filename).removeSuffix(".zip")
        val chatBucketInfo = findOrCreateChatIfNotExists(chatName)
        return chatBucketInfo.chatId to resource
    }

    private fun findOrCreateChatIfNotExists(chatName: String): ChatBucketInfo {
        val chatBucketInfo = chatRepository.findChatBucketInfoByChatName(chatName) ?: run {
            chatCreator.executeIfNotExists(NewChatInput(name = chatName))
            chatRepository.findChatBucketInfoByChatName(chatName)
        }

        return requireNotNull(chatBucketInfo) { "error when finding and trying to create chat not existent $chatName" }

    }
}