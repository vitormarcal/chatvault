package dev.marcal.chatvault.infrastructure.persistence.repository

import dev.marcal.chatvault.infrastructure.persistence.dto.ChatMessagePairDTO
import dev.marcal.chatvault.infrastructure.persistence.entity.ChatEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ChatCrudRepository : JpaRepository<ChatEntity, Long> {
    fun existsByExternalId(externalId: String): Boolean

    fun findByExternalId(externalId: String): ChatEntity

    fun findByName(chatName: String): ChatEntity?

    @Query(
        """
        SELECT new dev.marcal.chatvault.infrastructure.persistence.dto.ChatMessagePairDTO(c, m, (SELECT COUNT(*) FROM MessageEntity me WHERE me.chatId = c.id) )
        FROM ChatEntity c 
        JOIN MessageEntity m ON c.id = m.chatId  AND m.id = (SELECT MAX(m2.id) FROM MessageEntity m2 WHERE m2.chatId = c.id)
        ORDER BY m.createdAt desc
         """,
    )
    fun findAllChatsWithLastMessage(): List<ChatMessagePairDTO>

    @Modifying
    @Query("update ChatEntity c set c.name = :chatName where c.id = :chatId")
    fun updateChatNameByChatId(
        @Param("chatName") chatName: String,
        @Param("chatId") chatId: Long,
    )
}
