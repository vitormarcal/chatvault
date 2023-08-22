package dev.marcal.chatvault.persistence.repository

import dev.marcal.chatvault.persistence.entity.Chat
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ChatCrudRepository: JpaRepository<Chat, Long> {
}