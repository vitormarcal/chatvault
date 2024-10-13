package dev.marcal.chatvault.persistence.repository

import dev.marcal.chatvault.persistence.entity.ParameterEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ParameterCrudRepository : JpaRepository<ParameterEntity, String> {
}