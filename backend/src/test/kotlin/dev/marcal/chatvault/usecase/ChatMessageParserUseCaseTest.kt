package dev.marcal.chatvault.usecase

import dev.marcal.chatvault.domain.model.MessageParser
import dev.marcal.chatvault.ioboundary.output.MessageOutput
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class ChatMessageParserUseCaseTest {
    private val chatMessageParser = ChatMessageParserUseCase(MessageParser())

    @Test
    fun `when receive input stream should build messages`() {
        val expected =
            listOf(
                MessageOutput(
                    id = null,
                    author = "",
                    authorType = "SYSTEM",
                    attachmentName = null,
                    createdAt = LocalDateTime.of(2023, 8, 13, 18, 16),
                    content =
                        "As mensagens e as chamadas são protegidas com a criptografia de ponta a ponta e ficam somente entre você e os participantes desta conversa. Nem mesmo o WhatsApp pode ler ou ouvi-las. Toque para saber mais.",
                ),
                MessageOutput(
                    id = null,
                    author = "Fulano",
                    authorType = "USER",
                    attachmentName = null,
                    createdAt = LocalDateTime.of(2023, 8, 13, 17, 45),
                    content = "Que lindooos. Feliz dia dos pais, Beltrano!",
                ),
                MessageOutput(
                    id = null,
                    author = "Beltrano",
                    authorType = "USER",
                    attachmentName = null,
                    createdAt = LocalDateTime.of(2023, 8, 13, 18, 24),
                    content = "Opa, vlw Fulano !\uD83D\uDE43",
                ),
                MessageOutput(
                    id = null,
                    author = "Fulano",
                    authorType = "USER",
                    attachmentName = null,
                    createdAt = LocalDateTime.of(2023, 9, 19, 23, 3),
                    content = "Eita\nEssa ficou bonita",
                ),
            )

        val inputStream =
            """
            13/08/2023 18:16 - As mensagens e as chamadas são protegidas com a criptografia de ponta a ponta e ficam somente entre você e os participantes desta conversa. Nem mesmo o WhatsApp pode ler ou ouvi-las. Toque para saber mais.
            13/08/2023 17:45 - Fulano: Que lindooos. Feliz dia dos pais, Beltrano!
            13/08/2023 18:24 - Beltrano: Opa, vlw Fulano !🙃
            19/09/2023 23:03 - Fulano: Eita
            Essa ficou bonita
            """.trimIndent().byteInputStream()

        val list = chatMessageParser.parseToList(inputStream)

        assertEquals(expected, list)
    }

    @Test
    fun `when using custom pattern and receive input stream should build messages`() {
        val chatMessageParser = ChatMessageParserUseCase(MessageParser("[dd/MM/yyyy HH:mm]"))
        val expected =
            listOf(
                MessageOutput(
                    id = null,
                    author = "",
                    authorType = "SYSTEM",
                    attachmentName = null,
                    createdAt = LocalDateTime.of(2023, 8, 13, 18, 16),
                    content =
                        "As mensagens e as chamadas são protegidas com a criptografia de ponta a ponta e ficam somente entre você e os participantes desta conversa. Nem mesmo o WhatsApp pode ler ou ouvi-las. Toque para saber mais.",
                ),
                MessageOutput(
                    id = null,
                    author = "Fulano",
                    authorType = "USER",
                    attachmentName = null,
                    createdAt = LocalDateTime.of(2023, 8, 13, 17, 45),
                    content = "Que lindooos. Feliz dia dos pais, Beltrano!",
                ),
                MessageOutput(
                    id = null,
                    author = "Beltrano",
                    authorType = "USER",
                    attachmentName = null,
                    createdAt = LocalDateTime.of(2023, 8, 13, 18, 24),
                    content = "Opa, vlw Fulano !\uD83D\uDE43",
                ),
                MessageOutput(
                    id = null,
                    author = "Fulano",
                    authorType = "USER",
                    attachmentName = null,
                    createdAt = LocalDateTime.of(2023, 9, 19, 23, 3),
                    content = "Eita\nEssa ficou bonita",
                ),
            )

        val inputStream =
            """
            [13/08/2023 18:16] - As mensagens e as chamadas são protegidas com a criptografia de ponta a ponta e ficam somente entre você e os participantes desta conversa. Nem mesmo o WhatsApp pode ler ou ouvi-las. Toque para saber mais.
            [13/08/2023 17:45] - Fulano: Que lindooos. Feliz dia dos pais, Beltrano!
            [13/08/2023 18:24] - Beltrano: Opa, vlw Fulano !🙃
            [19/09/2023 23:03] - Fulano: Eita
            Essa ficou bonita
            """.trimIndent().byteInputStream()

        val list = chatMessageParser.parseToList(inputStream)

        assertEquals(expected, list)
    }

    @Test
    fun `when receive input stream should build message with attachmentName`() {
        val expected =
            listOf(
                MessageOutput(
                    id = null,
                    author = "Fulano",
                    authorType = "USER",
                    attachmentName = "IMG-20230922-WA0006.jpg",
                    createdAt = LocalDateTime.of(2023, 9, 22, 13, 33),
                    content = "IMG-20230922-WA0006.jpg (arquivo anexado)\nEsse é  um teste",
                ),
            )

        val inputStream =
            """
            22/09/2023 13:33 - Fulano: ‎IMG-20230922-WA0006.jpg (arquivo anexado)
            Esse é  um teste
            """.trimIndent().byteInputStream()

        val list = chatMessageParser.parseToList(inputStream)

        assertEquals(expected, list)
    }
}
