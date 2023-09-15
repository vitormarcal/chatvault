package dev.marcal.chatvault.service

import dev.marcal.chatvault.service.output.ChatLastMessageOutput

interface ListChat {

    fun execute(): List<ChatLastMessageOutput>

}