package dev.marcal.chatvault.ioboundary.input

data class NewChatInput(
    val name: String,
    val externalId: String? = null,
)
