package dev.marcal.chatvault.service

import org.springframework.core.io.Resource

interface ChatFileExporter {
    fun execute(chatId: Long): Resource

    fun executeAll(): Resource
}
