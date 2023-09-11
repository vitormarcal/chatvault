package dev.marcal.chatvault.persistence.repository

import dev.marcal.chatvault.persistence.entity.ChatEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ChatCrudRepository: JpaRepository<ChatEntity, Long> {

    fun existsByExternalId(externalId: String): Boolean

    fun findByExternalId(externalId: String): ChatEntity
}