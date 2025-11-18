package dev.marcal.chatvault.application.chat

import dev.marcal.chatvault.domain.bucket.BucketService
import dev.marcal.chatvault.domain.repository.ChatRepository
import org.springframework.stereotype.Service

@Service
class ChatDeletionService(
    private val chatRepository: ChatRepository,
    private val buckService: BucketService,
) {
    fun execute(chatId: Long) {
        chatRepository.findChatBucketInfoByChatId(chatId)?.let { chatBucketInfo ->
            buckService.delete(chatBucketInfo.bucket.toBucketFile())
            chatRepository.deleteChat(chatId)
        }
    }
}
