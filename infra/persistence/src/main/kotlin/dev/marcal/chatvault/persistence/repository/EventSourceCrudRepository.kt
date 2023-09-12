package dev.marcal.chatvault.persistence.repository

import dev.marcal.chatvault.persistence.entity.EventSourceEntity
import org.springframework.data.jpa.repository.JpaRepository

interface EventSourceCrudRepository: JpaRepository<EventSourceEntity, Long> {
}