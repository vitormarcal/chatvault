package dev.marcal.chatvault.usecase

import dev.marcal.chatvault.domain.bucketservice.BucketService
import dev.marcal.chatvault.domain.repository.ChatRepository
import dev.marcal.chatvault.service.ChatDeleter
import org.springframework.stereotype.Service

@Service
class ChatDeleterUseCase(
    private val chatRepository: ChatRepository,
    private val buckService: BucketService,
) : ChatDeleter {
    override fun execute(chatId: Long) {
        chatRepository.findChatBucketInfoByChatId(chatId)?.let { chatBucketInfo ->
            buckService.delete(chatBucketInfo.bucket.toBucketFile())
            chatRepository.deleteChat(chatId)
        }
    }
}
