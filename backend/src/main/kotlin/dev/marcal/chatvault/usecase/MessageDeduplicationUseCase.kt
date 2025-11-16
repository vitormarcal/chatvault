package dev.marcal.chatvault.usecase

import dev.marcal.chatvault.domain.repository.ChatRepository
import dev.marcal.chatvault.ioboundary.input.NewMessageInput
import dev.marcal.chatvault.usecase.mapper.toMessageDomain
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class MessageDeduplicationUseCase(
    private val chatRepository: ChatRepository,
) {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    fun execute(
        chatId: Long,
        messages: List<NewMessageInput>,
    ): List<NewMessageInput> {
        if (messages.isEmpty()) return emptyList()

        val chatBucketInfo = chatRepository.findChatBucketInfoByChatId(chatId) ?: return messages
        val lastSaved = chatRepository.findLastMessageByChatId(chatId) ?: return messages

        logger.info("starting message deduplication chatInfo=$chatBucketInfo, found ${messages.size} messages")

        messages[messages.size - 1].toMessageDomain(chatBucketInfo).also { lastInput ->
            if (lastInput == lastSaved || lastInput.createdAt < lastSaved.createdAt) {
                logger.info("messages are older than the last stored chatInfo=$chatBucketInfo")
                return emptyList()
            }
        }

        messages[0].toMessageDomain(chatBucketInfo).also { firstInput ->
            if (firstInput.createdAt > lastSaved.createdAt) return messages
        }

        var low = 0
        var high = messages.size - 1

        while (true) {
            val middle = (low + high).ushr(1)
            if (middle == low) {
                break
            }
            val middleMessage = messages[middle].toMessageDomain(chatBucketInfo)

            if (middleMessage == lastSaved) return messages.subList(middle + 1, messages.size)

            val compareTo = middleMessage.createdAt.compareTo(lastSaved.createdAt)

            if (compareTo < 0) {
                low = middle
            } else {
                high = middle
            }
        }

        return messages.subList(high, messages.size)
    }
}
