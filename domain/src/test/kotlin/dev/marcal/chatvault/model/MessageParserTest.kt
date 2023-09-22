package dev.marcal.chatvault.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class MessageParserTest {


    @Test
    fun `should return a system message`() {
        val line =
            "03/08/2023 18:16 - As mensagens e as chamadas são protegidas com a criptografia de ponta a ponta e ficam somente entre você e os participantes desta conversa. Nem mesmo o WhatsApp pode ler ou ouvi-las. Toque para saber mais."

        val message = MessageParser.parse(line) { it }
        val expected = Message(
            createdAt = LocalDateTime.of(2023, 8, 3, 18, 16),
            author = Author(
                name = "",
                type = AuthorType.SYSTEM
            ),
            externalId = null,
            content = Content(
                attachment = null,
                text = "As mensagens e as chamadas são protegidas com a criptografia de ponta a ponta e ficam somente entre você e os participantes desta conversa. Nem mesmo o WhatsApp pode ler ou ouvi-las. Toque para saber mais."
            )
        )


        assertEquals(expected, message)

    }

    @Test
    fun `should return a user message`() {
        val line =
            "13/08/2023 18:24 - Beltrano: Opa, vlw Fulano !\uD83D\uDE43"

        val message = MessageParser.parse(line) { it }
        val expected = Message(
            createdAt = LocalDateTime.of(2023, 8, 13, 18, 24),
            author = Author(
                name = "Beltrano",
                type = AuthorType.USER
            ),
            externalId = null,
            content = Content(
                attachment = null,
                text = "Opa, vlw Fulano !\uD83D\uDE43"
            )
        )


        assertEquals(expected, message)

    }

    @Test
    fun `should return a multiline message`() {
        val line = """
            19/09/2023 22:58 - Fulano: Olha aquela coisa!
            Vou pendurar na sala kkkk
            Hahahahah
        """.trimIndent()

        val message = MessageParser.parse(line) { it }
        val expected = Message(
            createdAt = LocalDateTime.of(2023, 9, 19, 22, 58),
            author = Author(
                name = "Fulano",
                type = AuthorType.USER
            ),
            externalId = null,
            content = Content(
                attachment = null,
                text = "Olha aquela coisa!\nVou pendurar na sala kkkk\nHahahahah"
            )
        )

        assertEquals(expected, message)

    }

    @Test
    fun `should return a exception when unexpected situation`() {
        val line =
            ""

        org.junit.jupiter.api.assertThrows<IllegalStateException> {
            val message = MessageParser.parse(line) { it }
        }

    }

    @Test
    fun `should mount message with attachmentName`() {
        val line =
            """
                22/09/2023 13:33 - Beltrano: ‎IMG-20230922-WA0006.jpg (arquivo anexado)
            """.trimIndent()
        val message = MessageParser.parse(line) { it }
        val expected = Message(
            createdAt = LocalDateTime.of(2023, 9, 22, 13, 33),
            author = Author(
                name = "Beltrano",
                type = AuthorType.USER
            ),
            externalId = null,
            content = Content(
                attachment = Attachment(name = "IMG-20230922-WA0006.jpg", bucket = Bucket(path = "/")),
                text = "IMG-20230922-WA0006.jpg (arquivo anexado)"
            )
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
        val message = MessageParser.parse(line) { it }
        val expected = Message(
            createdAt = LocalDateTime.of(2023, 9, 22, 13, 33),
            author = Author(
                name = "Beltrano",
                type = AuthorType.USER
            ),
            externalId = null,
            content = Content(
                attachment = Attachment(name = "IMG-20230922-WA0006.jpg", bucket = Bucket(path = "/")),
                text = "IMG-20230922-WA0006.jpg (arquivo anexado)\nEsse é  um teste"
            )
        )

        assertEquals(expected, message)
    }


}