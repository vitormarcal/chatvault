package dev.marcal.chatvault.application.chat

import dev.marcal.chatvault.api.dto.input.NewChatInput
import dev.marcal.chatvault.api.dto.mapper.toOutput
import dev.marcal.chatvault.api.dto.output.ChatBucketInfoOutput
import dev.marcal.chatvault.domain.model.Bucket
import dev.marcal.chatvault.domain.model.ChatPayload
import dev.marcal.chatvault.domain.repository.ChatRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class ChatCreatorService(
    private val chatRepository: ChatRepository,
) {
    fun executeIfNotExists(input: NewChatInput): ChatBucketInfoOutput {
        if (chatWithExternalIdExists(input.externalId)) {
            return chatRepository.findChatBucketInfoByExternalId(input.externalId!!).toOutput()
        }

        val chatToSave = buildChat(input)

        return chatRepository.create(chatToSave).toOutput()
    }

    private fun buildChat(input: NewChatInput): ChatPayload =
        ChatPayload(
            name = input.name,
            externalId = input.externalId,
            bucket =
                Bucket(
                    path = UUID.randomUUID().toString(),
                ),
        )

    private fun chatWithExternalIdExists(externalId: String?): Boolean = externalId != null && chatRepository.existsByExternalId(externalId)
}
