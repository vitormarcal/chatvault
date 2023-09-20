package dev.marcal.chatvault.service

import dev.marcal.chatvault.in_out_boundary.output.MessageOutput
import java.io.InputStream

interface ChatMessageParser {

    fun execute(inputStream: InputStream): Sequence<MessageOutput>

}