package dev.marcal.chatvault.model

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class MessageParser(val line: String) {
    private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

    private val dateWithoutNameRegex = "^(\\d{2}/\\d{2}/\\d{4} \\d{2}:\\d{2}) - (.*)$".toRegex()
    private val dateWithNameRegex = "^(\\d{2}/\\d{2}/\\d{4} \\d{2}:\\d{2}) - ([^:]+): (.+)$".toRegex()

    fun <R> parse(transform: (Message) -> R): R {
            return transform(parse(line))
    }

    private fun parse(
        line: String
    ): Message {
        val (date, name, content) = dateWithNameRegex.find(line)?.let { result ->
            val date = result.groupValues[1].trim().let { LocalDateTime.parse(it, formatter) }
            val name = result.groupValues[2].trim()
            val content = result.groupValues[3].trim()
            Triple(date, name, content)
        } ?: dateWithoutNameRegex.find(line)?.let { result ->
            val date = result.groupValues[1].trim().let { LocalDateTime.parse(it, formatter) }
            val content = result.groupValues[2].trim()
            Triple(date, null, content)

        } ?: throw IllegalStateException("situaão inesperada para a linha $line")

        return Message(
            author = name?.let { Author(name = it, type = AuthorType.USER) } ?: Author(
                name = "",
                type = AuthorType.SYSTEM
            ),
            content = Content(text = content, attachment = null),
            createdAt = date,
            externalId = null
        )
    }
}
