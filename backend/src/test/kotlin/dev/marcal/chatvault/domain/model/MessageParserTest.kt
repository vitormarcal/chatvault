package dev.marcal.chatvault.domain.model

import dev.marcal.chatvault.ioboundary.output.exceptions.MessageParserException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime

class MessageParserTest {
    @Test
    fun `should return a system message`() {
        val line =
            "15/08/2023 18:16 - As mensagens e as chamadas são protegidas com a criptografia de ponta a ponta e ficam " +
                "somente entre você e os participantes desta conversa. Nem mesmo o WhatsApp pode ler ou ouvi-las." +
                " Toque para saber mais."

        val message = MessageParser().parse(line) { it }
        val expected =
            Message(
                createdAt = LocalDateTime.of(2023, 8, 15, 18, 16),
                author =
                    Author(
                        name = "",
                        type = AuthorType.SYSTEM,
                    ),
                externalId = null,
                content =
                    Content(
                        attachment = null,
                        text =
                            "As mensagens e as chamadas são protegidas com a criptografia de ponta a ponta e ficam " +
                                "somente entre você e os participantes desta conversa. Nem mesmo o WhatsApp pode ler " +
                                "ou ouvi-las. Toque para saber mais.",
                    ),
            )

        assertEquals(expected, message)
    }

    @Test
    fun `should parse system message correctly with colon as separator`() {
        val line =
            "15/08/2023 18:16: As mensagens e as chamadas são protegidas com a criptografia de ponta a ponta e ficam" +
                " somente entre você e os participantes desta conversa. Nem mesmo o WhatsApp pode ler ou ouvi-las." +
                " Toque para saber mais."

        val message = MessageParser().parse(line) { it }
        val expected =
            Message(
                createdAt = LocalDateTime.of(2023, 8, 15, 18, 16),
                author =
                    Author(
                        name = "",
                        type = AuthorType.SYSTEM,
                    ),
                externalId = null,
                content =
                    Content(
                        attachment = null,
                        text =
                            "As mensagens e as chamadas são protegidas com a criptografia de ponta a ponta e ficam " +
                                "somente entre você e os participantes desta conversa. Nem mesmo o WhatsApp pode ler " +
                                "ou ouvi-las. Toque para saber mais.",
                    ),
            )

        assertEquals(expected, message)
    }

    @Test
    fun `should return a user message`() {
        val line =
            "13/08/2023 18:24 - Beltrano: Opa, vlw Fulano !\uD83D\uDE43"

        val message = MessageParser().parse(line) { it }
        val expected =
            Message(
                createdAt = LocalDateTime.of(2023, 8, 13, 18, 24),
                author =
                    Author(
                        name = "Beltrano",
                        type = AuthorType.USER,
                    ),
                externalId = null,
                content =
                    Content(
                        attachment = null,
                        text = "Opa, vlw Fulano !\uD83D\uDE43",
                    ),
            )

        assertEquals(expected, message)
    }

    @Test
    fun `should return a multiline message`() {
        val line =
            """
            19/09/2023 22:58 - Fulano: Olha aquela coisa!
            Vou pendurar na sala kkkk
            Hahahahah
            """.trimIndent()

        val message = MessageParser().parse(line) { it }
        val expected =
            Message(
                createdAt = LocalDateTime.of(2023, 9, 19, 22, 58),
                author =
                    Author(
                        name = "Fulano",
                        type = AuthorType.USER,
                    ),
                externalId = null,
                content =
                    Content(
                        attachment = null,
                        text = "Olha aquela coisa!\nVou pendurar na sala kkkk\nHahahahah",
                    ),
            )

        assertEquals(expected, message)
    }

    @Test
    fun `should parse message correctly with colon as separator`() {
        val line = "19/09/2023 22:58: Fulano: Olha aquela coisa!"

        val message = MessageParser().parse(line) { it }
        val expected =
            Message(
                createdAt = LocalDateTime.of(2023, 9, 19, 22, 58),
                author =
                    Author(
                        name = "Fulano",
                        type = AuthorType.USER,
                    ),
                externalId = null,
                content =
                    Content(
                        attachment = null,
                        text = "Olha aquela coisa!",
                    ),
            )

        assertEquals(expected, message)
    }

    @Test
    fun `should parse message correctly with dash as separator`() {
        val line = "19/09/2023 22:58 - Fulano: Olha aquela coisa!"

        val message = MessageParser().parse(line) { it }
        val expected =
            Message(
                createdAt = LocalDateTime.of(2023, 9, 19, 22, 58),
                author =
                    Author(
                        name = "Fulano",
                        type = AuthorType.USER,
                    ),
                externalId = null,
                content =
                    Content(
                        attachment = null,
                        text = "Olha aquela coisa!",
                    ),
            )

        assertEquals(expected, message)
    }

    @Test
    fun `should1 parse message correctly without date and name separator`() {
        val line = "[11/04/2013, 21:56:03] Name: \u200EName changed the group name to “my family”"

        val message = MessageParser("[dd/MM/yyyy, HH:mm:ss]").parse(line) { it }
        val expected =
            Message(
                createdAt = LocalDateTime.of(2013, 4, 11, 21, 56, 3),
                author =
                    Author(
                        name = "Name",
                        type = AuthorType.USER,
                    ),
                externalId = null,
                content =
                    Content(
                        attachment = null,
                        text = "Name changed the group name to “my family”",
                    ),
            )

        assertEquals(expected, message)
    }

    @Test
    fun `should parse message correctly when date has LRM character`() {
        val line = "\u200E19/09/2023 22:58 - Fulano: Olha aquela coisa!"

        val message = MessageParser().parse(line) { it }
        val expected =
            Message(
                createdAt = LocalDateTime.of(2023, 9, 19, 22, 58),
                author =
                    Author(
                        name = "Fulano",
                        type = AuthorType.USER,
                    ),
                externalId = null,
                content =
                    Content(
                        attachment = null,
                        text = "Olha aquela coisa!",
                    ),
            )

        assertEquals(expected, message)
    }

    @Test
    fun `should return a exception when unexpected situation`() {
        val line =
            ""

        assertThrows<MessageParserException> {
            val message = MessageParser().parse(line) { it }
            throw UnsupportedOperationException("test error returns $message")
        }
    }

    @Test
    fun `should mount message with attachmentName`() {
        val line =
            """
            22/09/2023 13:33 - Beltrano: ‎IMG-20230922-WA0006.jpg (arquivo anexado)
            """.trimIndent()
        val message = MessageParser().parse(line) { it }
        val expected =
            Message(
                createdAt = LocalDateTime.of(2023, 9, 22, 13, 33),
                author =
                    Author(
                        name = "Beltrano",
                        type = AuthorType.USER,
                    ),
                externalId = null,
                content =
                    Content(
                        attachment = Attachment(name = "IMG-20230922-WA0006.jpg", bucket = Bucket(path = "/")),
                        text = "IMG-20230922-WA0006.jpg (arquivo anexado)",
                    ),
            )

        assertEquals(expected, message)
    }

    @Test
    fun `should mount message without attachmentName`() {
        val line =
            """
            22/09/2023 13:33 - Beltrano: ‎IMG-20230922-WA0006.jpg (arquivo anexado) conforme anexo anterior
            """.trimIndent()
        val message = MessageParser().parse(line) { it }
        val expected =
            Message(
                createdAt = LocalDateTime.of(2023, 9, 22, 13, 33),
                author =
                    Author(
                        name = "Beltrano",
                        type = AuthorType.USER,
                    ),
                externalId = null,
                content =
                    Content(
                        attachment = null,
                        text = "IMG-20230922-WA0006.jpg (arquivo anexado) conforme anexo anterior",
                    ),
            )

        assertEquals(expected, message)
    }

    @Test
    fun `should mount message multi line with attachmentName`() {
        val line =
            """
            22/09/2023 13:33 - Beltrano: ‎IMG-20230922-WA0006.jpg (arquivo anexado)
            Esse é  um teste
            """.trimIndent()
        val message = MessageParser().parse(line) { it }
        val expected =
            Message(
                createdAt = LocalDateTime.of(2023, 9, 22, 13, 33),
                author =
                    Author(
                        name = "Beltrano",
                        type = AuthorType.USER,
                    ),
                externalId = null,
                content =
                    Content(
                        attachment = Attachment(name = "IMG-20230922-WA0006.jpg", bucket = Bucket(path = "/")),
                        text = "IMG-20230922-WA0006.jpg (arquivo anexado)\nEsse é  um teste",
                    ),
            )

        assertEquals(expected, message)
    }

    @Test
    fun `must identify date in the such format `() {
        listOf(
            "16/11/2023 18:44",
            "16/11/2023, 18:44",
            "11/16/23, 18:44",
            "23/16/11, 18:44",
            "16.11.23. 18:44",
            "16.11.2023. 18:44",
            "16.11.23, 18:44",
            "2023.11.16, 18:44",
            "2/22/24, 12:06",
            "30.09.14, 23:25",
            "30.09.14, 23:25:46",
            "2/22/24, 12:06:01",
        ).forEach {
            assertEquals(it, MessageParser().extractTextDate(it))
            assertEquals(it, MessageParser().extractTextDate("${it}xyz"))
            assertEquals("\u200E$it", MessageParser().extractTextDate("\u200E$it"))
            assertEquals(null, MessageParser().extractTextDate("\u200Exyz${it}xyz"))
            assertEquals(null, MessageParser().extractTextDate("xyz${it}xyz"))
            assertEquals(null, MessageParser().extractTextDate("xyz ${it}xyz"))
            assertEquals("$it am", MessageParser().extractTextDate("$it am"))
            assertEquals("$it am", MessageParser().extractTextDate("$it am"))
            assertEquals("${it}am", MessageParser().extractTextDate("${it}am"))
            assertEquals("$it pm", MessageParser().extractTextDate("$it pm"))
            assertEquals("${it}pm", MessageParser().extractTextDate("${it}pm"))
            assertEquals("[${it}pm]", MessageParser().extractTextDate("[${it}pm]"))
            assertEquals("[${it}am]", MessageParser().extractTextDate("[${it}am]"))
            assertEquals("[$it]", MessageParser().extractTextDate("[$it]"))
            assertEquals("[$it]", MessageParser().extractTextDate("[$it]"))
            assertEquals("[$it]", MessageParser().extractTextDate("[$it]"))
        }
    }

    @Test
    fun `must parse date string with order dd mm yy(yy) to localdatetime`() {
        assertEquals(LocalDateTime.of(2023, 11, 16, 18, 44), MessageParser().parseDate("16/11/2023 18:44"))
        assertEquals(LocalDateTime.of(2023, 11, 16, 18, 44), MessageParser().parseDate("16/11/2023, 18:44"))
        assertEquals(LocalDateTime.of(2023, 11, 16, 18, 44), MessageParser().parseDate("16/11/23, 18:44"))
        assertEquals(LocalDateTime.of(2023, 11, 16, 18, 44), MessageParser().parseDate("16.11.23. 18:44"))
        assertEquals(LocalDateTime.of(2023, 11, 16, 18, 44), MessageParser().parseDate("16. 11. 23. 18:44"))
        assertEquals(LocalDateTime.of(2023, 11, 16, 18, 44), MessageParser().parseDate("16/ 11/ 23/ 18:44"))
        assertEquals(LocalDateTime.of(2023, 11, 16, 18, 44), MessageParser().parseDate("16-11-23 18:44"))
        assertEquals(LocalDateTime.of(2023, 11, 16, 18, 44), MessageParser().parseDate("[16-11-23 18:44]"))
        assertEquals(LocalDateTime.of(2024, 2, 22, 12, 6), MessageParser().parseDate("2/22/24, 12:06"))
    }

    @Test
    fun `based on previous resolved dates, should resolve ambiguity`() {
        MessageParser().apply {
            assertEquals(LocalDateTime.of(2023, 1, 16, 18, 44), this.parseDate("01/16/2023 18:44"))
            assertEquals(LocalDateTime.of(2024, 2, 22, 12, 6), this.parseDate("2/22/24, 12:06"))
            assertEquals(LocalDateTime.of(2023, 1, 1, 18, 44), this.parseDate("01/01/2023 18:44"))
            assertEquals(LocalDateTime.of(2023, 2, 1, 18, 44), this.parseDate("02/01/2023 18:44"))
            assertEquals(LocalDateTime.of(2023, 3, 1, 18, 44), this.parseDate("03/01/2023 18:44"))
            assertEquals(LocalDateTime.of(2023, 4, 1, 18, 44), this.parseDate("04/01/2023 18:44"))
            assertEquals(LocalDateTime.of(2023, 5, 1, 18, 44), this.parseDate("05/01/2023 18:44"))
            assertEquals(LocalDateTime.of(2023, 6, 1, 18, 44), this.parseDate("06/01/2023 18:44"))

            assertEquals(LocalDateTime.of(2023, 1, 16, 18, 44), this.parseDate("16/01/2023 18:44"))
            assertEquals(LocalDateTime.of(2023, 1, 1, 18, 44), this.parseDate("01/01/2023 18:44"))
            assertEquals(LocalDateTime.of(2023, 2, 1, 18, 44), this.parseDate("01/02/2023 18:44"))
            assertEquals(LocalDateTime.of(2023, 3, 1, 18, 44), this.parseDate("01/03/2023 18:44"))
            assertEquals(LocalDateTime.of(2023, 4, 1, 18, 44), this.parseDate("01/04/2023 18:44"))
            assertEquals(LocalDateTime.of(2023, 5, 1, 18, 44), this.parseDate("01/05/2023 18:44"))
            assertEquals(LocalDateTime.of(2023, 6, 1, 18, 44), this.parseDate("01/06/2023 18:44"))
        }
    }

    @Test
    fun `should parse with days or months with 1 char date pattern `() {
        MessageParser().apply {
            assertEquals(LocalDateTime.of(2023, 1, 16, 18, 44), this.parseDate("1/16/2023 18:44"))
            assertEquals(LocalDateTime.of(2023, 1, 1, 18, 44), this.parseDate("1/1/2023 18:44"))
            assertEquals(LocalDateTime.of(2024, 2, 22, 12, 6), this.parseDate("2/22/24, 12:06"))
        }
    }

    @Test
    fun `should use custom date pattern `() {
        MessageParser("MM/dd/yyyy HH:mm").apply {
            assertEquals(LocalDateTime.of(2023, 1, 1, 18, 44), this.parseDate("01/01/2023 18:44"))
            assertEquals(LocalDateTime.of(2023, 2, 1, 18, 44), this.parseDate("02/01/2023 18:44"))
            assertEquals(LocalDateTime.of(2023, 3, 1, 18, 44), this.parseDate("03/01/2023 18:44"))
            assertEquals(LocalDateTime.of(2023, 4, 1, 18, 44), this.parseDate("04/01/2023 18:44"))
            assertEquals(LocalDateTime.of(2023, 5, 1, 18, 44), this.parseDate("05/01/2023 18:44"))
            assertEquals(LocalDateTime.of(2023, 6, 1, 18, 44), this.parseDate("06/01/2023 18:44"))
        }

        MessageParser("MM/dd/yyyy HH:mm:ss").apply {
            assertEquals(LocalDateTime.of(2023, 1, 1, 18, 44, 25), this.parseDate("01/01/2023 18:44:25"))
            assertEquals(LocalDateTime.of(2023, 2, 1, 18, 44, 25), this.parseDate("02/01/2023 18:44:25"))
            assertEquals(LocalDateTime.of(2023, 3, 1, 18, 44, 25), this.parseDate("03/01/2023 18:44:25"))
            assertEquals(LocalDateTime.of(2023, 4, 1, 18, 44, 25), this.parseDate("04/01/2023 18:44:25"))
            assertEquals(LocalDateTime.of(2023, 5, 1, 18, 44, 25), this.parseDate("05/01/2023 18:44:25"))
            assertEquals(LocalDateTime.of(2023, 6, 1, 18, 44, 25), this.parseDate("06/01/2023 18:44:25"))
        }

        MessageParser("M/dd/yy, HH:mm").apply {
            assertEquals(LocalDateTime.of(2024, 2, 22, 12, 6), this.parseDate("2/22/24, 12:06"))
            assertEquals(LocalDateTime.of(2024, 2, 22, 12, 6), this.parseDate("02/22/24, 12:06"))
        }

        MessageParser("dd/MM/yyyy HH:mm").apply {
            assertEquals(LocalDateTime.of(2023, 1, 1, 18, 44), this.parseDate("01/01/2023 18:44"))
            assertEquals(LocalDateTime.of(2023, 2, 1, 18, 44), this.parseDate("01/02/2023 18:44"))
            assertEquals(LocalDateTime.of(2023, 3, 1, 18, 44), this.parseDate("01/03/2023 18:44"))
            assertEquals(LocalDateTime.of(2023, 4, 1, 18, 44), this.parseDate("01/04/2023 18:44"))
            assertEquals(LocalDateTime.of(2023, 5, 1, 18, 44), this.parseDate("01/05/2023 18:44"))
            assertEquals(LocalDateTime.of(2023, 6, 1, 18, 44), this.parseDate("01/06/2023 18:44"))
        }

        MessageParser("[dd/MM/yyyy HH:mm]").apply {
            assertEquals(LocalDateTime.of(2023, 1, 1, 18, 44), this.parseDate("[01/01/2023 18:44]"))
            assertEquals(LocalDateTime.of(2023, 2, 1, 18, 44), this.parseDate("[01/02/2023 18:44]"))
            assertEquals(LocalDateTime.of(2023, 3, 1, 18, 44), this.parseDate("[01/03/2023 18:44]"))
            assertEquals(LocalDateTime.of(2023, 4, 1, 18, 44), this.parseDate("[01/04/2023 18:44]"))
            assertEquals(LocalDateTime.of(2023, 5, 1, 18, 44), this.parseDate("[01/05/2023 18:44]"))
            assertEquals(LocalDateTime.of(2023, 6, 1, 18, 44), this.parseDate("[01/06/2023 18:44]"))
        }

        MessageParser("[dd][MM][yyyy][HH]:[mm]").apply {
            assertEquals(LocalDateTime.of(2023, 1, 1, 18, 44), this.parseDate("[01][01][2023][18]:[44]"))
            assertEquals(LocalDateTime.of(2023, 2, 1, 18, 44), this.parseDate("[01][02][2023][18]:[44]"))
            assertEquals(LocalDateTime.of(2023, 3, 1, 18, 44), this.parseDate("[01][03][2023][18]:[44]"))
            assertEquals(LocalDateTime.of(2023, 4, 1, 18, 44), this.parseDate("[01][04][2023][18]:[44]"))
            assertEquals(LocalDateTime.of(2023, 5, 1, 18, 44), this.parseDate("[01][05][2023][18]:[44]"))
            assertEquals(LocalDateTime.of(2023, 6, 1, 18, 44), this.parseDate("[01][06][2023][18]:[44]"))
        }

        MessageParser(" dd*MM*yyyy***hh:mm**a ").apply {
            assertEquals(LocalDateTime.of(2023, 1, 1, 10, 44), this.parseDate(" 01*01*2023***10:44**AM "))
            assertEquals(LocalDateTime.of(2023, 2, 1, 10, 44), this.parseDate(" 01*02*2023***10:44**AM "))
            assertEquals(LocalDateTime.of(2023, 3, 1, 10, 44), this.parseDate(" 01*03*2023***10:44**AM "))
            assertEquals(LocalDateTime.of(2023, 4, 1, 10, 44), this.parseDate(" 01*04*2023***10:44**AM "))
            assertEquals(LocalDateTime.of(2023, 5, 1, 10, 44), this.parseDate(" 01*05*2023***10:44**AM "))
            assertEquals(LocalDateTime.of(2023, 6, 1, 10, 44), this.parseDate(" 01*06*2023***10:44**AM "))
        }
    }

    @Test
    fun `If there was no previous resolution of ambiguity, throw an exception when ambiguity occurs`() {
        MessageParser().apply {
            assertThrows<RuntimeException> { this.parseDate("01/01/2023 18:44") }
            assertThrows<RuntimeException> { this.parseDate("02/01/2023 18:44") }
            assertThrows<RuntimeException> { this.parseDate("01/03/2023 18:44") }
            assertThrows<RuntimeException> { this.parseDate("01/04/2023 18:44") }
            assertThrows<RuntimeException> { this.parseDate("05/04/2023 18:44") }
        }
    }
}
