package dev.marcal.chatvault.usecase

import dev.marcal.chatvault.app_service.bucket_service.BucketService
import dev.marcal.chatvault.in_out_boundary.output.exceptions.AttachmentFinderException
import dev.marcal.chatvault.model.BucketFile
import dev.marcal.chatvault.repository.ChatRepository
import dev.marcal.chatvault.service.ProfileImageManager
import org.springframework.context.ApplicationContext
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import java.io.InputStream

@Service
class ProfileImageManagerUseCase(
    private val bucketService: BucketService,
    private val chatRepository: ChatRepository,
    private val context: ApplicationContext
) : ProfileImageManager {
    override fun updateImage(inputStream: InputStream, chatId: Long) {
        val bucketInfo =
            chatRepository.findChatBucketInfoByChatId(chatId) ?: throw RuntimeException("chatId not found")
        bucketService.save(
            BucketFile(
                stream = inputStream,
                fileName = "profile-image.jpg",
                address = bucketInfo.bucket.withPath("/")
            )
        )
    }

    override fun getImage(chatId: Long): Resource {
        val bucketInfo =
            chatRepository.findChatBucketInfoByChatId(chatId) ?: throw RuntimeException("chatId not found")

        return try {
            bucketService.loadFileAsResource(
                BucketFile(
                    fileName = "profile-image.jpg",
                    address = bucketInfo.bucket.withPath("/")
                )
            )
        } catch (ex: AttachmentFinderException) {
            context.getResource("classpath:public/default-avatar.png")
        }
    }
}