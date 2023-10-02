package dev.marcal.chatvault.in_out_boundary.input

data class NewChatInput(
    val name: String,
    val externalId: String? = null
)
