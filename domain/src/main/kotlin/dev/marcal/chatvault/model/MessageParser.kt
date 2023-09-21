package dev.marcal.chatvault.model

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


object MessageParser {
    private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

    private val dateWithoutNameRegex = "^(\\d{2}/\\d{2}/\\d{4} \\d{2}:\\d{2}) - (.*)$".toRegex()
    private val dateWithNameRegex = "^(\\d{2}/\\d{2}/\\d{4} \\d{2}:\\d{2}) - ([^:]+): (.+)$".toRegex()
    private val onlyDate = "\\d{2}/\\d{2}/\\d{4} \\d{2}:\\d{2}".toRegex()

    fun <R> parse(text: String, transform: (Message) -> R): R {
        return transform(parse(text))
    }

    fun extractDate(text: String): LocalDateTime? {
        val dateMatcher = onlyDate.find(text)
        return dateMatcher?.value?.let { LocalDateTime.parse(it, formatter) }
    }

    private fun parse(
        text: String
    ): Message {
        val (firstLine, textMessage) = text.split("\n").let {lines ->
            lines.first() to lines.drop(1).joinToString("\n")
        }
        val (date, name, firstLineMessage) = dateWithNameRegex.find(firstLine)?.let { result ->
            val date = result.groupValues[1].trim().let { LocalDateTime.parse(it, formatter) }
            val name = result.groupValues[2].trim()
            val content = result.groupValues[3].trim()
            Triple(date, name, content)
        } ?: dateWithoutNameRegex.find(text)?.let { result ->
            val date = result.groupValues[1].trim().let { LocalDateTime.parse(it, formatter) }
            val content = result.groupValues[2].trim()
            Triple(date, null, content)

        } ?: throw IllegalStateException("situaão inesperada para a linha $firstLine")

        val content = (firstLineMessage.removePrefix("\u200E") + (textMessage.takeIf { it.isNotEmpty() }?.let { "\n" + it } ?: ""))

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
