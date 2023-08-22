package dev.marcal.chatvault.persistence.repository

import dev.marcal.chatvault.persistence.entity.Message
import org.springframework.data.jpa.repository.JpaRepository

interface MessageCrudRepository: JpaRepository<Message, Long> {
}