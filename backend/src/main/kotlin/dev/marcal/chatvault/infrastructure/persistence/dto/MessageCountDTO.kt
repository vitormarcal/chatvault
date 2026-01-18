package dev.marcal.chatvault.infrastructure.persistence.dto

import java.time.LocalDate

data class MessageCountDTO(
    val date: LocalDate,
    val count: Long,
)
