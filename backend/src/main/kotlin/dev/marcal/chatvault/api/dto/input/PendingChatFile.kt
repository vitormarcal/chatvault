package dev.marcal.chatvault.api.dto.input

import java.io.InputStream

class PendingChatFile(
    val stream: InputStream,
    val fileName: String,
    val chatName: String,
)
