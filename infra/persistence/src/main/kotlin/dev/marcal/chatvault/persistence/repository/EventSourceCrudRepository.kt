package dev.marcal.chatvault.persistence.repository

import dev.marcal.chatvault.persistence.entity.EventSourceEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional

interface EventSourceCrudRepository: JpaRepository<EventSourceEntity, Long> {

    @Query(
        """
            SELECT e FROM EventSourceEntity e
            WHERE 0=0
                AND e.chatId = :chatId
                AND e.imported <> true
                AND e.externalId is not null
        """
    )
    fun findLegacyNotImportedByChatId(@Param("chatId") chatId: Long, pageable: Pageable): Page<EventSourceEntity>

    @Query(
        """
            SELECT DISTINCT e.chatId FROM EventSourceEntity e
        """
    )
    fun findAllChatId(): List<Long>

    @Modifying
    @Transactional
    @Query(
        """
            UPDATE EventSourceEntity e
            SET e.imported = true
            WHERE e.externalId = :externalId
        """
    )
    fun setImportedTrue(@Param("externalId") externalId: String)
}