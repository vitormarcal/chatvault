package dev.marcal.chatvault.api.dto.output

import java.time.LocalDate

data class MessageStatisticsOutput(
    val month: Int,
    val year: Int,
    val statistics: List<DateStatisticOutput>,
    val minDate: LocalDate,
    val maxDate: LocalDate,
    val isDataDense: Boolean,
)
