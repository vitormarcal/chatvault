package dev.marcal.chatvault.usecase

import dev.marcal.chatvault.domain.bucketservice.BucketService
import dev.marcal.chatvault.domain.model.BucketFile
import dev.marcal.chatvault.domain.repository.ChatRepository
import dev.marcal.chatvault.ioboundary.output.exceptions.AttachmentFinderException
import dev.marcal.chatvault.ioboundary.output.exceptions.ChatNotFoundException
import dev.marcal.chatvault.service.ProfileImageManager
import org.springframework.context.ApplicationContext
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import java.io.InputStream

@Service
class ProfileImageManagerUseCase(
    private val bucketService: BucketService,
    private val chatRepository: ChatRepository,
    private val context: ApplicationContext,
) : ProfileImageManager {
    override fun updateImage(
        inputStream: InputStream,
        chatId: Long,
    ) {
        val bucketInfo =
            chatRepository.findChatBucketInfoByChatId(chatId)
                ?: throw ChatNotFoundException("Unable to update chat image because chat $chatId was not found")
        bucketService.save(
            BucketFile(
                stream = inputStream,
                fileName = "profile-image.jpg",
                address = bucketInfo.bucket.withPath("/"),
            ),
        )
    }

    override fun getImage(chatId: Long): Resource {
        val bucketInfo =
            chatRepository.findChatBucketInfoByChatId(chatId)
                ?: throw ChatNotFoundException("Unable to retrieve chat image because chat $chatId was not found")

        return try {
            bucketService.loadFileAsResource(
                BucketFile(
                    fileName = "profile-image.jpg",
                    address = bucketInfo.bucket.withPath("/"),
                ),
            )
        } catch (ex: AttachmentFinderException) {
            context.getResource("classpath:public/default-avatar.png")
        }
    }
}
