package dev.marcal.chatvault.usecase

import dev.marcal.chatvault.in_out_boundary.output.MessageOutput
import dev.marcal.chatvault.model.MessageParser
import dev.marcal.chatvault.service.ChatMessageParser
import dev.marcal.chatvault.usecase.mapper.toOutput
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
class ChatMessageParserUseCase(
) : ChatMessageParser {

    private val messageParser = MessageParser()
    override fun <R> parseAndTransform(
        inputStream: InputStream,
        transformIn: (MessageOutput) -> R
    ): List<R> {
        return runBlocking { parse(inputStream).map { transformIn(it) }.toList() }
    }

    override fun parseToList(
        inputStream: InputStream
    ): List<MessageOutput> {
        return parseAndTransform(inputStream) { it }
    }

    override fun parse(inputStream: InputStream): Flow<MessageOutput> {
        return sequenceOfTextMessage(inputStream)
            .map { messageText -> messageParser.parse(messageText) { it.toOutput() } }
    }

    private fun sequenceOfTextMessage(inputStream: InputStream): Flow<String> {
        val message = callbackFlow {
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