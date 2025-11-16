package dev.marcal.chatvault.service

interface ChatDeleter {
    fun execute(chatId: Long)
}
