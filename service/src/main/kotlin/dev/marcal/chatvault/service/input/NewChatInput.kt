package dev.marcal.chatvault.service.input

data class NewChatInput(
    val name: String,
    val externalId: String? = null
)
