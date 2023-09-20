package dev.marcal.chatvault.usecase

import dev.marcal.chatvault.in_out_boundary.output.MessageOutput
import dev.marcal.chatvault.model.MessageParser
import dev.marcal.chatvault.service.ChatMessageParser
import dev.marcal.chatvault.usecase.mapper.toOutput
import org.springframework.stereotype.Service
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

@Service
class ChatMessageParserUseCase(
) : ChatMessageParser {

    override fun execute(inputStream: InputStream): Sequence<MessageOutput> {

        val inputStreamReader = InputStreamReader(inputStream)
        val reader = BufferedReader(inputStreamReader)

        val messagesSequence = generateSequence {
            val line = reader.readLine()
            if (line != null) {
                MessageParser(line).parse { it.toOutput() }
            } else {
                reader.close()
                inputStreamReader.close()
                inputStream.close()
                null
            }

        }

        return messagesSequence


    }
}