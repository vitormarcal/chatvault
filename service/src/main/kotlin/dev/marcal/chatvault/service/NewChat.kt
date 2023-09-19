package dev.marcal.chatvault.service

import dev.marcal.chatvault.in_out_boundary.input.NewChatInput
import dev.marcal.chatvault.in_out_boundary.output.ChatBucketInfoOutput

interface NewChat {

    fun executeIfNotExists(input: NewChatInput): ChatBucketInfoOutput
}