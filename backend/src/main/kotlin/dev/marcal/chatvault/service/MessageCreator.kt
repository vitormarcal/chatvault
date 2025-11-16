package dev.marcal.chatvault.service

import dev.marcal.chatvault.ioboundary.input.NewMessageInput
import dev.marcal.chatvault.ioboundary.input.NewMessagePayloadInput

interface MessageCreator {
    fun execute(input: NewMessageInput)

    fun execute(input: NewMessagePayloadInput)
}
