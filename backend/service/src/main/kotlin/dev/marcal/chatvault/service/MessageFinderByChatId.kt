package dev.marcal.chatvault.service

import dev.marcal.chatvault.in_out_boundary.output.MessageOutput
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface MessageFinderByChatId {

    fun execute(chatId: Long, query: String?, pageable: Pageable): Page<MessageOutput>

    fun execute(chatId: Long): Sequence<MessageOutput>
}