package dev.marcal.chatvault.service

import dev.marcal.chatvault.service.input.NewMessageInput
import dev.marcal.chatvault.service.input.NewMessagePayloadInput

interface NewMessage {
    fun execute(input: NewMessageInput)
    fun execute(input: NewMessagePayloadInput)
}