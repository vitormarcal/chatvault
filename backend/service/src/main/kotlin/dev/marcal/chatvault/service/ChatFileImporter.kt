package dev.marcal.chatvault.service

import java.io.InputStream

interface ChatFileImporter {
    fun execute(chatId: Long, inputStream: InputStream, fileType: String)
    fun execute(chatName: String?, inputStream: InputStream, fileType: String)
}