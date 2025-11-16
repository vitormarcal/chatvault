package dev.marcal.chatvault.domain.model

object ChatNamePatternMatcher {
    private val whatsappPattern = Regex(".*WhatsApp.*")
    private val chatPattern = Regex("(^|.*/)_chat\\.txt$")

    /**
     * Matcher for identifying WhatsApp or chat file patterns.
     * - Matches filenames containing 'WhatsApp' anywhere.
     * - Matches '_chat.txt' files with or without a preceding directory.
     *
     * @param input the filename or path to check
     * @return true if the input matches the WhatsApp or chat file pattern, false otherwise
     */
    fun matches(input: String): Boolean = matchesWhatsApp(input) || matchesChatFile(input)

    private fun matchesWhatsApp(input: String): Boolean = whatsappPattern.containsMatchIn(input)

    private fun matchesChatFile(input: String): Boolean = chatPattern.containsMatchIn(input)
}
