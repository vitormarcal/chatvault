package dev.marcal.chatvault.model

import dev.marcal.chatvault.in_out_boundary.output.exceptions.AmbiguousDateException
import dev.marcal.chatvault.in_out_boundary.output.exceptions.MessageParserException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder


class MessageParser(pattern: String? = null) {
    private val customFormatter: DateTimeFormatter? =
        pattern?.let { DateTimeFormatter.ofPattern(it.removeBracketsAndTrim()) }
    private val firstComesTheDayFormatter: DateTimeFormatter = buildWithPattern("[dd.MM.yyyy][dd.MM.yy]")
    private val firstComesTheMonthFormatter: DateTimeFormatter = buildWithPattern("[MM.dd.yyyy][MM.dd.yy]")

    private var lastUsed: DateTimeFormatter? = null

    private val dateRegexText =
        "^(\\[?\\d{1,4}[-/.]\\d{1,4}[-/.]\\d{1,4}[,.]? \\d{2}:\\d{2}(?::\\d{2})?\\s?([aA][mM]|[pP][mM])?\\]?)"
    private val dateWithoutNameRegex = "$dateRegexText(?: - |: )(.*)$".toRegex()
    private val dateWithNameRegex = "$dateRegexText(?: - |: )([^:]+): (.+)$".toRegex()
    private val onlyDate = dateRegexText.toRegex()
    private val attachmentNameRegex = "^(.*?)\\s+\\((.*?)\\)$".toRegex()

    fun <R> parse(text: String, transform: (Message) -> R): R {
        return transform(parse(text))
    }

    private fun buildWithPattern(pattern: String): DateTimeFormatter {
        return DateTimeFormatterBuilder().parseCaseSensitive().appendPattern(pattern).optionalStart()
            .appendPattern("[,][.]").optionalEnd().appendPattern("[hh:mma][HH:mm]").toFormatter()
    }

    fun parseDate(text: String): LocalDateTime {
        if (customFormatter != null) {
            return LocalDateTime.parse(text.removeBracketsAndTrim(), customFormatter)
        }
        return tryToInfer(text)
    }

    private fun tryToInfer(text: String): LocalDateTime {
        text.removeBracketsAndTrim()
            .sanitizeWithDots()
            .let {
                val groups = it.split(".")
                val textToParse = groups.dayAndMonthWith2Digits() ?: it
                if (groups[0].toInt() > 12) {
                    lastUsed = firstComesTheDayFormatter
                    return LocalDateTime.parse(textToParse, firstComesTheDayFormatter)
                } else if (groups[1].toInt() > 12) {
                    lastUsed = firstComesTheMonthFormatter
                    return LocalDateTime.parse(textToParse, firstComesTheMonthFormatter)
                } else {
                    if (lastUsed == null) {
                        throw AmbiguousDateException("There is ambiguity in the date, it is not possible to know which value is the day and which is the month $text")
                    } else {
                        return LocalDateTime.parse(textToParse, lastUsed)
                    }
                }
            }
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

        return Message(author = name?.let { Author(name = it, type = AuthorType.USER) } ?: Author(
            name = "", type = AuthorType.SYSTEM
        ),
            content = Content(text = removeTrailingNulls(content), attachment = attachment),
            createdAt = date,
            externalId = null)
    }

    private fun extractDateNameFirstLineMessage(
        firstLine: String, text: String
    ): Triple<LocalDateTime, String?, String> {
        return dateWithNameRegex.find(firstLine)?.let { result ->
            val date = parseDate(result.groupValues[1])
            val name = result.groupValues[3].trim()
            val content = result.groupValues[4].trim()
            Triple(date, name, removePrefix(content))
        } ?: dateWithoutNameRegex.find(text)?.let { result ->
            val date = parseDate(result.groupValues[1])
            val content = result.groupValues[3].trim()
            Triple(date, null, removePrefix(content))

        } ?: throw MessageParserException("Parse text fail. Unexpected situation for the line $firstLine")
    }

    private fun removePrefix(content: String): String {
        return content.removePrefix("\u200E")
    }

    private fun removeTrailingNulls(content: String): String {
        return content.filterNot { it == '\u0000' }
    }
}

fun String.removeBracketsAndTrim(): String = this.replace("[\\[\\]]+".toRegex(), " ").trim()

fun String.sanitizeWithDots(): String = this.replace("[.\\s,\\-/]+".toRegex(), ".")

fun List<String>.dayAndMonthWith2Digits(): String? {

    val first = if (this[0].length == 1) "0" + this[0] else null
    val second = if (this[1].length == 1) "0" + this[1] else null
    if (first == null && second == null) {
        return null
    }
    val mutable = this.toMutableList()
    first?.let { mutable[0] = it }
    second?.let { mutable[1] = it }
    return mutable.joinToString(".")
}
