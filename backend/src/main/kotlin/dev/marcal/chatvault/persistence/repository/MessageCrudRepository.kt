package dev.marcal.chatvault.persistence.repository

import dev.marcal.chatvault.persistence.dto.AttachmentInfoDTO
import dev.marcal.chatvault.persistence.entity.MessageEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface MessageCrudRepository : JpaRepository<MessageEntity, Long> {
    fun countByChatId(chatId: Long): Long

    @Query(
        """
        SELECT m FROM MessageEntity m 
        WHERE 0=0 
            AND m.chatId = :chatId
            AND (:query = '' 
                  OR ((LOWER(m.author) LIKE LOWER(CONCAT('%', :query, '%')) 
                  OR LOWER(m.content) LIKE LOWER(CONCAT('%', :query, '%')))))
    """,
    )
    fun findAllByChatIdIs(
        @Param("query") query: String,
        @Param("chatId") chatId: Long,
        pageable: Pageable,
    ): Page<MessageEntity>

    fun findMessageEntityByIdAndChatId(
        id: Long,
        chatId: Long,
    ): MessageEntity?

    fun findTopByChatIdOrderByIdDesc(chatId: Long): MessageEntity?

    @Query(
        "SELECT new dev.marcal.chatvault.persistence.dto.AttachmentInfoDTO(m.id, m.attachmentName) FROM MessageEntity m WHERE m.chatId = :chatId AND m.attachmentPath IS NOT NULL",
    )
    fun findMessageIdByChatIdAndAttachmentExists(chatId: Long): List<AttachmentInfoDTO>

    fun deleteAllByChatId(chatId: Long)
}
