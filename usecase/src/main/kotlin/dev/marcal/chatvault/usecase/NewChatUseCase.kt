package dev.marcal.chatvault.usecase

import dev.marcal.chatvault.model.Bucket
import dev.marcal.chatvault.model.Chat
import dev.marcal.chatvault.model.ChatPayload
import dev.marcal.chatvault.repository.ChatRepository
import dev.marcal.chatvault.service.NewChat
import dev.marcal.chatvault.service.input.NewChatInput
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class NewChatUseCase(
    private val chatRepository: ChatRepository
): NewChat {
    override fun executeIfNotExists(input: NewChatInput) {

        if (chatWithExternalIdExists(input.externalId)) {
            return
        }

        val chatToSave = buildChat(input)

        chatRepository.create(chatToSave)

    }

    private fun buildChat(input: NewChatInput): ChatPayload {
        return ChatPayload(
            name = input.name,
            externalId = input.externalId,
            bucket = Bucket(
                path = UUID.randomUUID().toString()
            )
        )
    }

    private fun chatWithExternalIdExists(externalId: String?): Boolean {
        return externalId != null && chatRepository.existsByExternalId(externalId)
    }
}