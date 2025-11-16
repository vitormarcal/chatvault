package dev.marcal.chatvault.service

import dev.marcal.chatvault.ioboundary.output.MessageOutput
import kotlinx.coroutines.flow.Flow
import java.io.InputStream

interface ChatMessageParser {
    fun parse(inputStream: InputStream): Flow<MessageOutput>

    fun parseToList(inputStream: InputStream): List<MessageOutput>

    fun <R> parseAndTransform(
        inputStream: InputStream,
        transformIn: (MessageOutput) -> R,
    ): List<R>
}
