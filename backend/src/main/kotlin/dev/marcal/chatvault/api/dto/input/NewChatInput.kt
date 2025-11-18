package dev.marcal.chatvault.api.dto.input

data class NewChatInput(
    val name: String,
    val externalId: String? = null,
)
