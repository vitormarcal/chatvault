package dev.marcal.chatvault.model

object ChatNamePatternMatcher {
    private val possiblyWhatsappTalk = Regex("(.*WhatsApp.*)|(^_chat\\.txt$)")

    fun matches(input: String): Boolean {
        return possiblyWhatsappTalk.containsMatchIn(input)
    }
}