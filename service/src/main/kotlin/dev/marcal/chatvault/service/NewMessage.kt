package dev.marcal.chatvault.service

import dev.marcal.chatvault.service.input.NewMessageInput

interface NewMessage {
    fun execute(input: NewMessageInput)
}