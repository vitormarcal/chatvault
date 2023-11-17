package dev.marcal.chatvault.persistence.repository

import dev.marcal.chatvault.in_out_boundary.output.MessageOutput
import dev.marcal.chatvault.persistence.dto.AttachmentInfoDTO
import dev.marcal.chatvault.persistence.entity.MessageEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface MessageCrudRepository : JpaRepository<MessageEntity, Long> {

    fun countByChatId(chatId: Long): Long
    fun findAllByChatIdIs(chatId: Long, pageable: Pageable): Page<MessageOutput>

    fun findMessageEntityByIdAndChatId(id: Long, chatId: Long): MessageEntity?

    fun findTopByChatId(chatId: Long): MessageEntity?

    @Query("SELECT new dev.marcal.chatvault.persistence.dto.AttachmentInfoDTO(m.id, m.attachmentName) FROM MessageEntity m WHERE m.chatId = :chatId AND m.attachmentPath IS NOT NULL")
    fun findMessageIdByChatIdAndAttachmentExists(chatId: Long): List<AttachmentInfoDTO>
}