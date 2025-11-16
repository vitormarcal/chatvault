package dev.marcal.chatvault.domain.model

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import kotlin.text.matches

class ChatNamePatternMatcherTest {
    companion object {
        @JvmStatic
        fun validPatternStrings() =
            listOf(
                "_chat.txt", // Exact match for _chat.txt
                "folder/_chat.txt", // Exact match for dir with  _chat.txt
                "Conversa com WhatsApp.txt", // Contains WhatsApp
                "Conversation with WhatsApp.txt", // Contains WhatsApp
            )

        @JvmStatic
        fun invalidPatternStrings() =
            listOf(
                "Outras mensagens", // Does not contain WhatsApp or _chat.txt
                "Este é um arquivo _chato.txt", // Does not end with _chat.txt
                "mensagem aleatória", // No patterns
                "arquivo_chatx.txt", // Incorrect pattern (error at the end)
                "arquivo_com_chat e outro conteudo", // Does not end with .txt
                "arquivo_chat.txt", // Ends with _chat.txt but is not the exact pattern
                "dir/arquivo_chat.txt", // Ends with _chat.txt but is not the exact pattern
            )
    }

    @ParameterizedTest
    @MethodSource("validPatternStrings")
    fun `should return true for valid pattern strings`(input: String) {
        assertTrue(ChatNamePatternMatcher.matches(input), "Expected '$input' to match the pattern")
    }

    @ParameterizedTest
    @MethodSource("invalidPatternStrings")
    fun `should return false for invalid pattern strings`(input: String) {
        assertFalse(ChatNamePatternMatcher.matches(input), "Expected '$input' not to match the pattern")
    }
}
