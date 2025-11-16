package dev.marcal.chatvault.service

import dev.marcal.chatvault.ioboundary.input.NewChatInput
import dev.marcal.chatvault.ioboundary.output.ChatBucketInfoOutput

interface ChatCreator {
    fun executeIfNotExists(input: NewChatInput): ChatBucketInfoOutput
}
