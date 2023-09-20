package dev.marcal.chatvault.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class MessageParserTest {


    @Test
    fun `should return a system message`() {
        val line =
            "03/08/2023 18:16 - As mensagens e as chamadas são protegidas com a criptografia de ponta a ponta e ficam somente entre você e os participantes desta conversa. Nem mesmo o WhatsApp pode ler ou ouvi-las. Toque para saber mais."

        val message = MessageParser(line).parse { it }
        val expected = Message(
            createdAt = LocalDateTime.of(2023,8,3,18,16),
            author = Author(
                name = "",
                type = AuthorType.SYSTEM
            ),
            externalId = null,
            content = Content(
                attachment = null,
                text = "As mensagens e as chamadas são protegidas com a criptografia de ponta a ponta e ficam somente entre você e os participantes desta conversa. Nem mesmo o WhatsApp pode ler ou ouvi-las. Toque para saber mais.")
        )


        assertEquals(expected, message)

    }

    @Test
    fun `should return a user message`() {
        val line =
            "13/08/2023 18:24 - Beltrano: Opa, vlw Fulano !\uD83D\uDE43"

        val message = MessageParser(line).parse { it }
        val expected = Message(
            createdAt = LocalDateTime.of(2023,8,13,18,24),
            author = Author(
                name = "Beltrano",
                type = AuthorType.USER
            ),
            externalId = null,
            content = Content(
                attachment = null,
                text = "Opa, vlw Fulano !\uD83D\uDE43")
        )


        assertEquals(expected, message)

    }

    @Test
    fun `should return a exception when unexpected situation`() {
        val line =
            ""

        org.junit.jupiter.api.assertThrows<IllegalStateException> {
            MessageParser(line).parse { message ->  message }
        }

    }


}