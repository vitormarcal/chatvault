package dev.marcal.chatvault.service

import org.springframework.core.io.Resource

interface ChatFileExporter {

    fun execute(chatId: Long): Resource
    fun execute(chatId: List<Long>): Resource
}