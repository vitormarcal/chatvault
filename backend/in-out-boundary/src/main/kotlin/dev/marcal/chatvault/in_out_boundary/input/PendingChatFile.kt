package dev.marcal.chatvault.in_out_boundary.input

import java.io.InputStream

class PendingChatFile(
    val stream: InputStream,
    val fileName: String,
    val chatName: String
)