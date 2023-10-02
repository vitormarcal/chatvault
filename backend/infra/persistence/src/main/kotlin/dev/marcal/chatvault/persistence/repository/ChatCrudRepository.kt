package dev.marcal.chatvault.persistence.repository

import dev.marcal.chatvault.persistence.dto.ChatMessagePairDTO
import dev.marcal.chatvault.persistence.entity.ChatEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ChatCrudRepository: JpaRepository<ChatEntity, Long> {

    fun existsByExternalId(externalId: String): Boolean

    fun findByExternalId(externalId: String): ChatEntity

    fun findByName(chatName: String): ChatEntity?


    @Query(
        """
        SELECT new dev.marcal.chatvault.persistence.dto.ChatMessagePairDTO(c, m)
        FROM ChatEntity c 
        JOIN MessageEntity m ON c.id = m.chatId  AND m.id = (SELECT MAX(m2.id) FROM MessageEntity m2 WHERE m2.chatId = c.id)
         """
    )
    fun findAllChatsWithLastMessage(): List<ChatMessagePairDTO>
}