package dev.marcal.chatvault.model

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


object MessageParser {
    private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

    private const val DATE_REGEX_TEXT =
        "^(\\d{2,4}[-/.]\\d{2,4}[-/.]\\d{2,4}[,.]? \\d{2}:\\d{2}\\s?([aA][mM]|[pP][mM])?)"
    private val dateWithoutNameRegex = "$DATE_REGEX_TEXT - (.*)$".toRegex()
    private val dateWithNameRegex = "$DATE_REGEX_TEXT - ([^:]+): (.+)$".toRegex()
    private val onlyDate = DATE_REGEX_TEXT.toRegex()
    private val attachmentNameRegex = "^(.*?)\\s+\\((.*?)\\)$".toRegex()

    fun <R> parse(text: String, transform: (Message) -> R): R {
        return transform(parse(text))
    }

    fun extractDate(text: String): LocalDateTime? {
        val dateMatcher = onlyDate.find(text)
        return dateMatcher?.value?.let { LocalDateTime.parse(it, formatter) }
    }

    fun extractTextDate(text: String): String? {
        val dateMatcher = onlyDate.find(text)
        return dateMatcher?.value
    }

    private fun parse(
        text: String
    ): Message {
        val (firstLine, textMessage) = text.split("\n").let { lines ->
            lines.first() to lines.drop(1).joinToString("\n")
        }
        val (date, name, firstLineMessage) = extractDateNameFirstLineMessage(firstLine, text)

        val content = (firstLineMessage + (textMessage.takeIf { it.isNotEmpty() }?.let { "\n" + it } ?: ""))

        val attachment = attachmentNameRegex.find(firstLineMessage)?.groupValues?.get(1)?.let {
            Attachment(name = it, bucket = Bucket("/"))
        }

        return Message(
            author = name?.let { Author(name = it, type = AuthorType.USER) } ?: Author(
                name = "",
                type = AuthorType.SYSTEM
            ),
            content = Content(text = removeTrailingNulls(content), attachment = attachment),
            createdAt = date,
            externalId = null
        )
    }

    private fun extractDateNameFirstLineMessage(
        firstLine: String,
        text: String
    ): Triple<LocalDateTime, String?, String> {
        return dateWithNameRegex.find(firstLine)?.let { result ->
            val date = result.groupValues[1].trim().let { LocalDateTime.parse(it, formatter) }
            val name = result.groupValues[3].trim()
            val content = result.groupValues[4].trim()
            Triple(date, name, removePrefix(content))
        } ?: dateWithoutNameRegex.find(text)?.let { result ->
            val date = result.groupValues[1].trim().let { LocalDateTime.parse(it, formatter) }
            val content = result.groupValues[3].trim()
            Triple(date, null, removePrefix(content))

        } ?: throw IllegalStateException("situaão inesperada para a linha $firstLine")
    }

    private fun removePrefix(content: String): String {
        return content.removePrefix("\u200E")
    }

    private fun removeTrailingNulls(content: String): String {
        return content.filterNot { it == '\u0000' }
    }
}
