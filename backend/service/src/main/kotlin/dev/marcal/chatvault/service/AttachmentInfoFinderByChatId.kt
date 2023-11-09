package dev.marcal.chatvault.service

import dev.marcal.chatvault.in_out_boundary.output.AttachmentInfoOutput

interface AttachmentInfoFinderByChatId {
    fun execute(chatId: Long): Sequence<AttachmentInfoOutput>
}