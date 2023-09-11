package dev.marcal.chatvault.service

import dev.marcal.chatvault.service.input.NewChatInput

interface NewChat {

    fun executeIfNotExists(input: NewChatInput)
}