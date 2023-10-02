package dev.marcal.chatvault.service

import dev.marcal.chatvault.in_out_boundary.output.ChatLastMessageOutput

interface ChatLister {

    fun execute(): List<ChatLastMessageOutput>

}