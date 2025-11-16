package dev.marcal.chatvault.domain.model

import dev.marcal.chatvault.ioboundary.output.exceptions.AmbiguousDateException
import dev.marcal.chatvault.ioboundary.output.exceptions.MessageParserException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder

class MessageParser(
    pattern: String? = null,
) {
    private val customFormatter: DateTimeFormatter? =
        pattern?.let { DateTimeFormatter.ofPattern(it.removeBracketsAndTrim()) }
    private val firstComesTheDayFormatter: DateTimeFormatter by lazy {
        buildWithPattern("[dd.MM.yyyy][dd.MM.yy]")
    }
    private val firstComesTheMonthFormatter: DateTimeFormatter by lazy {
        buildWithPattern("[MM.dd.yyyy][MM.dd.yy]")
    }

    private var lastUsed: DateTimeFormatter? = null

    companion object {
        private const val UNICODE_LEFT_TO_RIGHT = "\u200E"
        private const val NULL_CHAR = '\u0000'

        private const val DATE_REGEX =
            "^$UNICODE_LEFT_TO_RIGHT?(\\[?\\d{1,4}[-/.]\\d{1,4}[-/.]\\d{1,4}[,.]? \\d{2}:\\d{2}(?::\\d{2})?\\s?([aA][mM]|[pP][mM])?\\]?)"
        private val DATE_WITHOUT_NAME_REGEX = "$DATE_REGEX(?: - |: )(.*)$".toRegex()
        private val DATE_WITH_NAME_REGEX = "$DATE_REGEX(?: - |: |\\s+)([^:]+): (.+)$".toRegex()
        private val ONLY_DATE = DATE_REGEX.toRegex()
        private val ATTACHMENT_NAME_REGEX = "^(.*?)\\s+\\((.*?)\\)$".toRegex()
    }

    fun <R> parse(
        text: String,
        transform: (Message) -> R,
    ): R = transform(parse(text))

    private fun buildWithPattern(pattern: String): DateTimeFormatter =
        DateTimeFormatterBuilder()
            .parseCaseSensitive()
            .appendPattern(pattern)
            .optionalStart()
            .appendPattern("[,][.]")
            .optionalEnd()
            .appendPattern("[hh:mma][HH:mm]")
            .toFormatter()

    fun parseDate(text: String): LocalDateTime =
        customFormatter?.let {
            LocalDateTime.parse(text.removeBracketsAndTrim(), it)
        } ?: tryToInfer(text)

    /**
     * Tries to infer the date from the provided text based on pre-configured formats.
     * Throws [AmbiguousDateException] if the day and month cannot be distinguished.
     */
    private fun tryToInfer(text: String): LocalDateTime {
        text
            .removeBracketsAndTrim()
            .normalizeToDotSeparatedFormat()
            .let {
                val groups = it.split(".")
                val textToParse = groups.dayAndMonthWith2Digits() ?: it
                val formatter =
                    when {
                        groups[0].toInt() > 12 -> firstComesTheDayFormatter
                        groups[1].toInt() > 12 -> firstComesTheMonthFormatter
                        lastUsed != null -> lastUsed
                        else -> throw AmbiguousDateException(
                            "There is ambiguity in the date, it is not possible to know which value is the day and which is the month: $text",
                        )
                    }
                lastUsed = formatter!!
                return LocalDateTime.parse(textToParse, formatter)
            }
    }

    fun extractTextDate(text: String): String? {
        val dateMatcher = ONLY_DATE.find(text)
        return dateMatcher?.value
    }

    private fun parse(text: String): Message {
        val firstLine = text.lineSequence().first()
        val textMessage = text.lineSequence().drop(1).joinToString("\n")

        val (date, name, firstLineMessage) = extractDateNameFirstLineMessage(firstLine, text)

        val content = buildContent(firstLineMessage, textMessage)
        val attachment = extractAttachment(firstLineMessage)

        return Message(
            author = buildAuthor(name),
            content = Content(text = content.removeNullCharacters(), attachment = attachment),
            createdAt = date,
            externalId = null,
        )
    }

    private fun extractDateNameFirstLineMessage(
        firstLine: String,
        text: String,
    ): ParsedMessageInfo =
        DATE_WITH_NAME_REGEX.find(firstLine)?.let { result ->
            val date = parseDate(result.groupValues[1])
            val name = result.groupValues[3].trim()
            val content = result.groupValues[4].trim()
            ParsedMessageInfo(date, name, content.removeLtrPrefix())
        } ?: DATE_WITHOUT_NAME_REGEX.find(text)?.let { result ->
            val date = parseDate(result.groupValues[1])
            val content = result.groupValues[3].trim()
            ParsedMessageInfo(date, null, content.removeLtrPrefix())
        } ?: throw MessageParserException("Parse text fail. Unexpected situation for the line $firstLine")

    private fun buildAuthor(name: String?): Author =
        name?.let {
            Author(name = it, type = AuthorType.USER)
        } ?: Author(name = "", type = AuthorType.SYSTEM)

    private fun buildContent(
        firstLineMessage: String,
        textMessage: String,
    ): String =
        listOf(firstLineMessage, textMessage)
            .filter {
                it.isNotEmpty()
            }.joinToString("\n")

    private fun extractAttachment(firstLineMessage: String): Attachment? =
        ATTACHMENT_NAME_REGEX.find(firstLineMessage)?.groupValues?.get(1)?.let {
            Attachment(name = it, bucket = Bucket("/"))
        }

    private fun String.removeLtrPrefix() = this.removePrefix(UNICODE_LEFT_TO_RIGHT)

    private fun String.removeNullCharacters() = this.filterNot { it == NULL_CHAR }

    data class ParsedMessageInfo(
        val date: LocalDateTime,
        val name: String?,
        val content: String,
    )
}

fun String.removeBracketsAndTrim(): String = this.replace("[\\[\\]]+".toRegex(), " ").trim()

fun String.normalizeToDotSeparatedFormat(): String = this.replace("[.\\s,\\-/]+".toRegex(), ".")

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
