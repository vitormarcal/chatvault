package dev.marcal.chatvault.application.message

import dev.marcal.chatvault.api.dto.mapper.toOutput
import dev.marcal.chatvault.api.dto.output.MessageOutput
import dev.marcal.chatvault.domain.model.MessageParser
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

@Service
class ChatMessageParserService(
    private val messageParser: MessageParser,
) {
    fun <R> parseAndTransform(
        inputStream: InputStream,
        transformIn: (MessageOutput) -> R,
    ): List<R> = runBlocking { parse(inputStream).map { transformIn(it) }.toList() }

    fun parseToList(inputStream: InputStream): List<MessageOutput> = parseAndTransform(inputStream) { it }

    fun parse(inputStream: InputStream): Flow<MessageOutput> =
        sequenceOfTextMessage(inputStream)
            .map { messageText -> messageParser.parse(messageText) { it.toOutput() } }

    private fun sequenceOfTextMessage(inputStream: InputStream): Flow<String> {
        val message =
            callbackFlow {
                val reader = BufferedReader(InputStreamReader(inputStream))
                var currentDate: String? = null
                var currentLines = StringBuilder()

                reader.forEachLine { line ->

                    messageParser.extractTextDate(line)?.let { lineDate ->
                        if (currentDate != null && currentLines.isNotEmpty()) {
                            trySendBlocking(currentLines)
                        }
                        currentDate = lineDate
                        currentLines = StringBuilder(line)
                    } ?: currentLines.appendLine().append(line)
                }

                if (currentDate != null && currentLines.isNotEmpty()) {
                    trySendBlocking(currentLines)
                }
                close()
            }
        return message
    }

    private fun ProducerScope<String>.trySendBlocking(currentLines: StringBuilder) {
        trySendBlocking(currentLines.toString()).takeIf { it.isFailure }?.getOrThrow()
    }
}
