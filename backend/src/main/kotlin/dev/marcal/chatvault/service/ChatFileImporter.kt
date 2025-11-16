package dev.marcal.chatvault.service

import dev.marcal.chatvault.ioboundary.input.FileTypeInputEnum
import java.io.InputStream

interface ChatFileImporter {
    fun execute(
        chatId: Long,
        inputStream: InputStream,
        fileType: FileTypeInputEnum,
    )

    fun execute(
        chatName: String?,
        inputStream: InputStream,
        fileType: FileTypeInputEnum,
    )
}
