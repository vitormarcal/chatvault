package dev.marcal.chatvault.ioboundary.input

import java.io.InputStream

class PendingChatFile(
    val stream: InputStream,
    val fileName: String,
    val chatName: String,
)
