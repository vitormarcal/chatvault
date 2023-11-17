package dev.marcal.chatvault.usecase

import dev.marcal.chatvault.in_out_boundary.input.NewMessageInput
import dev.marcal.chatvault.repository.ChatRepository
import dev.marcal.chatvault.usecase.mapper.toMessageDomain
import org.springframework.stereotype.Service

@Service
class MessageDeduplicatorUseCase(
    private val chatRepository: ChatRepository
) {

    fun execute(chatId: Long, messages: List<NewMessageInput>): List<NewMessageInput> {

        if (messages.isEmpty()) return emptyList()

        val chatBucketInfo = chatRepository.findChatBucketInfoByChatId(chatId) ?: return messages
        val lastSaved = chatRepository.findLastMessageByChatId(chatId) ?: return messages

        messages[messages.size - 1].toMessageDomain(chatBucketInfo).also { lastInput ->
            if (lastInput == lastSaved || lastInput.createdAt < lastSaved.createdAt) return emptyList()
        }


        var low = 0
        var high = messages.size - 1
        var continueLoop = true
        var cont = 1


        while (continueLoop) {
            println("${cont++} passada")
            val middle = (low + high).ushr(1)
            if (middle == low) {
                continueLoop = false
                continue
            }
            val middleMessage = messages[middle].toMessageDomain(chatBucketInfo)

            if (middleMessage == lastSaved) return messages.subList(middle + 1, messages.size)

            val compareTo = middleMessage.createdAt.compareTo(lastSaved.createdAt)

            if (compareTo == -1) {
                low = middle
            } else {
                high = middle
            }
        }

        return messages.subList(high, messages.size)
    }
}