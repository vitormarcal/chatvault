package dev.marcal.chatvault.application.message

import dev.marcal.chatvault.api.dto.output.DateStatisticOutput
import dev.marcal.chatvault.api.dto.output.MessageStatisticsOutput
import dev.marcal.chatvault.infrastructure.persistence.repository.MessageCrudRepository
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class MessageStatisticsService(
    private val messageCrudRepository: MessageCrudRepository,
) {
    fun getMonthlyStatistics(
        chatId: Long,
        year: Int,
        month: Int,
    ): MessageStatisticsOutput {
        val counts = messageCrudRepository.findMessageCountsByChatIdAndMonth(chatId, year, month)

        val statistics =
            counts.map {
                DateStatisticOutput(date = it.date, messageCount = it.count)
            }

        val minDate =
            counts.minByOrNull { it.date }?.date
                ?: LocalDate.of(year, month, 1)
        val maxDate =
            counts.maxByOrNull { it.date }?.date
                ?: LocalDate.of(year, month, 1)

        // Dense if more than 15 days have messages
        val isDataDense = statistics.size > 15

        return MessageStatisticsOutput(
            month = month,
            year = year,
            statistics = statistics,
            minDate = minDate,
            maxDate = maxDate,
            isDataDense = isDataDense,
        )
    }
}
