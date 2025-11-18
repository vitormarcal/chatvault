package dev.marcal.chatvault.application.chat

import dev.marcal.chatvault.api.web.exception.AttachmentFinderException
import dev.marcal.chatvault.api.web.exception.ChatNotFoundException
import dev.marcal.chatvault.domain.bucket.BucketService
import dev.marcal.chatvault.domain.model.BucketFile
import dev.marcal.chatvault.domain.repository.ChatRepository
import org.springframework.context.ApplicationContext
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import java.io.InputStream

@Service
class ProfileImageService(
    private val bucketService: BucketService,
    private val chatRepository: ChatRepository,
    private val context: ApplicationContext,
) {
    fun updateImage(
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

    fun getImage(chatId: Long): Resource {
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
