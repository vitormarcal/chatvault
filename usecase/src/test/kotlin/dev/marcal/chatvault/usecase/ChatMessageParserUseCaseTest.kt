package dev.marcal.chatvault.usecase

import dev.marcal.chatvault.in_out_boundary.output.MessageOutput
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class ChatMessageParserUseCaseTest {

    private val chatMessageParser = ChatMessageParserUseCase()
    @Test
    fun `when receive input stream should build messages`() {

        val expected = listOf(
            MessageOutput(
                id = null,
                author = "",
                authorType = "SYSTEM",
                attachmentName = null,
                createdAt = LocalDateTime.of(2023,8,3,18,16),
                content = "As mensagens e as chamadas são protegidas com a criptografia de ponta a ponta e ficam somente entre você e os participantes desta conversa. Nem mesmo o WhatsApp pode ler ou ouvi-las. Toque para saber mais."
            ),
            MessageOutput(
                id = null,
                author = "Fulano",
                authorType = "USER",
                attachmentName = null,
                createdAt = LocalDateTime.of(2023,8,13,17,45),
                content = "Que lindooos. Feliz dia dos pais, Beltrano!"
            ),
            MessageOutput(
                id = null,
                author = "Beltrano",
                authorType = "USER",
                attachmentName = null,
                createdAt = LocalDateTime.of(2023,8,13,18,24),
                content = "Opa, vlw Fulano !\uD83D\uDE43"
            )
        )

        val inputStream = """
            03/08/2023 18:16 - As mensagens e as chamadas são protegidas com a criptografia de ponta a ponta e ficam somente entre você e os participantes desta conversa. Nem mesmo o WhatsApp pode ler ou ouvi-las. Toque para saber mais.
            13/08/2023 17:45 - Fulano: Que lindooos. Feliz dia dos pais, Beltrano!
            13/08/2023 18:24 - Beltrano: Opa, vlw Fulano !🙃
        """.trimIndent().byteInputStream()

        val list = chatMessageParser.execute(inputStream).toList()


        assertEquals(expected, list)
    }

}