package dev.marcal.chatvault.service

import dev.marcal.chatvault.in_out_boundary.input.FileTypeInputEnum
import java.io.InputStream

interface ChatFileImporter {
    fun execute(chatId: Long, inputStream: InputStream, fileType: FileTypeInputEnum)
    fun execute(chatName: String?, inputStream: InputStream, fileType: FileTypeInputEnum)
}