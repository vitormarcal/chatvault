package dev.marcal.chatvault.infrastructure.persistence.repository

import dev.marcal.chatvault.infrastructure.persistence.dto.AttachmentInfoDTO
import dev.marcal.chatvault.infrastructure.persistence.dto.MessageCountDTO
import dev.marcal.chatvault.infrastructure.persistence.entity.MessageEntity
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
        "SELECT new dev.marcal.chatvault.infrastructure.persistence.dto.AttachmentInfoDTO(m.id, m.attachmentName) FROM MessageEntity m WHERE m.chatId = :chatId AND m.attachmentPath IS NOT NULL",
    )
    fun findMessageIdByChatIdAndAttachmentExists(chatId: Long): List<AttachmentInfoDTO>

    fun deleteAllByChatId(chatId: Long)

    @Query(
        """
        SELECT m FROM MessageEntity m
        WHERE m.chatId = :chatId
            AND m.createdAt >= :targetDate
        ORDER BY m.createdAt ASC, m.id ASC
        """,
    )
    fun findFirstMessageAtOrAfterDate(
        @Param("chatId") chatId: Long,
        @Param("targetDate") targetDate: java.time.LocalDateTime,
        pageable: Pageable,
    ): Page<MessageEntity>

    @Query(
        """
        SELECT new dev.marcal.chatvault.infrastructure.persistence.dto.MessageCountDTO(
            CAST(m.createdAt AS java.time.LocalDate),
            COUNT(m.id)
        )
        FROM MessageEntity m
        WHERE m.chatId = :chatId
            AND YEAR(m.createdAt) = :year
            AND MONTH(m.createdAt) = :month
        GROUP BY CAST(m.createdAt AS java.time.LocalDate)
        ORDER BY CAST(m.createdAt AS java.time.LocalDate) ASC
        """,
    )
    fun findMessageCountsByChatIdAndMonth(
        @Param("chatId") chatId: Long,
        @Param("year") year: Int,
        @Param("month") month: Int,
    ): List<MessageCountDTO>
}
