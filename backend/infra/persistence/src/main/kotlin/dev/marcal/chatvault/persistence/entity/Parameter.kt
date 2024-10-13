package dev.marcal.chatvault.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.ColumnTransformer

@Entity
@Table(name = "parameter")
data class ParameterEntity(
    @Id
    val name: String,
    @Column(columnDefinition = "jsonb")   @ColumnTransformer(write = "?::jsonb") val config: String? = null,
)