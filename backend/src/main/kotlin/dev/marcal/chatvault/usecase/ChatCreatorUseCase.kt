package dev.marcal.chatvault.usecase

import dev.marcal.chatvault.domain.model.Bucket
import dev.marcal.chatvault.domain.model.ChatPayload
import dev.marcal.chatvault.domain.repository.ChatRepository
import dev.marcal.chatvault.ioboundary.input.NewChatInput
import dev.marcal.chatvault.ioboundary.output.ChatBucketInfoOutput
import dev.marcal.chatvault.service.ChatCreator
import dev.marcal.chatvault.usecase.mapper.toOutput
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class ChatCreatorUseCase(
    private val chatRepository: ChatRepository,
) : ChatCreator {
    override fun executeIfNotExists(input: NewChatInput): ChatBucketInfoOutput {
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
