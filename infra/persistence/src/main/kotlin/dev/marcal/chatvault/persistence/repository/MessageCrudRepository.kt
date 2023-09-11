package dev.marcal.chatvault.persistence.repository

import dev.marcal.chatvault.persistence.entity.MessageEntity
import org.springframework.data.jpa.repository.JpaRepository

interface MessageCrudRepository: JpaRepository<MessageEntity, Long> {
}