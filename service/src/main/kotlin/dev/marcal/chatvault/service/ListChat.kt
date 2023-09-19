package dev.marcal.chatvault.service

import dev.marcal.chatvault.in_out_boundary.output.ChatLastMessageOutput

interface ListChat {

    fun execute(): List<ChatLastMessageOutput>

}