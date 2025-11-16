package dev.marcal.chatvault.service

import dev.marcal.chatvault.ioboundary.output.AttachmentInfoOutput

interface AttachmentInfoFinderByChatId {
    fun execute(chatId: Long): Sequence<AttachmentInfoOutput>
}
