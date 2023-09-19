package dev.marcal.chatvault.usecase

import dev.marcal.chatvault.model.Bucket
import dev.marcal.chatvault.model.ChatPayload
import dev.marcal.chatvault.repository.ChatRepository
import dev.marcal.chatvault.service.NewChat
import dev.marcal.chatvault.in_out_boundary.input.NewChatInput
import dev.marcal.chatvault.in_out_boundary.output.ChatBucketInfoOutput
import dev.marcal.chatvault.usecase.mapper.toOutput
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class NewChatUseCase(
    private val chatRepository: ChatRepository
): NewChat {
    override fun executeIfNotExists(input: NewChatInput): ChatBucketInfoOutput {

        if (chatWithExternalIdExists(input.externalId)) {
            return chatRepository.findChatBucketInfoByExternalId(input.externalId!!).toOutput()
        }

        val chatToSave = buildChat(input)

        return chatRepository.create(chatToSave).toOutput()

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