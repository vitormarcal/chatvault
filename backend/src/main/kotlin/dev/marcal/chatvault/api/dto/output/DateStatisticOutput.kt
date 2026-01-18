package dev.marcal.chatvault.api.dto.output

import java.time.LocalDate

data class DateStatisticOutput(
    val date: LocalDate,
    val messageCount: Long,
)
