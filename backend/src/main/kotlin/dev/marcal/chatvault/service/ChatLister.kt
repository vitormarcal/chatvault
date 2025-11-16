package dev.marcal.chatvault.service

import dev.marcal.chatvault.ioboundary.output.ChatLastMessageOutput

interface ChatLister {
    fun execute(): List<ChatLastMessageOutput>
}
