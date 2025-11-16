package dev.marcal.chatvault.service

interface ChatNameUpdater {
    fun execute(
        chatId: Long,
        chatName: String,
    )
}
