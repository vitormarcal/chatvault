package dev.marcal.chatvault.service

import dev.marcal.chatvault.in_out_boundary.input.NewMessageInput
import dev.marcal.chatvault.in_out_boundary.input.NewMessagePayloadInput

interface MessageCreator {
    fun execute(input: NewMessageInput)
    fun execute(input: NewMessagePayloadInput)
}