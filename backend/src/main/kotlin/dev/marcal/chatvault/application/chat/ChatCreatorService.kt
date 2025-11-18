package dev.marcal.chatvault.application.chat

import dev.marcal.chatvault.api.dto.input.NewChatInput
import dev.marcal.chatvault.domain.model.Bucket
import dev.marcal.chatvault.domain.model.ChatBucketInfo
import dev.marcal.chatvault.domain.model.ChatPayload
import dev.marcal.chatvault.domain.repository.ChatRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class ChatCreatorService(
    private val chatRepository: ChatRepository,
) {
    fun createIfNotExists(input: NewChatInput): ChatBucketInfo {
        if (chatWithExternalIdExists(input.externalId)) {
            return chatRepository.findChatBucketInfoByExternalId(input.externalId!!)
        }

        val chatToSave =
            ChatPayload(
                name = input.name,
                externalId = input.externalId,
                bucket = Bucket(path = UUID.randomUUID().toString()),
            )

        return chatRepository.create(chatToSave)
    }

    fun findOrCreateByName(chatName: String): ChatBucketInfo =
        chatRepository.findChatBucketInfoByChatName(chatName)
            ?: createIfNotExists(NewChatInput(name = chatName))

    private fun chatWithExternalIdExists(externalId: String?): Boolean = externalId != null && chatRepository.existsByExternalId(externalId)
}
