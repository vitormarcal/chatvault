package dev.marcal.chatvault.service

import dev.marcal.chatvault.service.input.NewChatInput
import dev.marcal.chatvault.service.output.ChatBucketInfoOutput

interface NewChat {

    fun executeIfNotExists(input: NewChatInput): ChatBucketInfoOutput
}