package dev.marcal.chatvault.persistence.repository

import dev.marcal.chatvault.in_out_boundary.output.MessageOutput
import dev.marcal.chatvault.persistence.entity.MessageEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface MessageCrudRepository: JpaRepository<MessageEntity, Long> {

    fun findAllByChatIdIs(chatId: Long, pageable: Pageable): Page<MessageOutput>

    fun findMessageEntityByIdAndChatId(id: Long, chatId: Long): MessageEntity?
}