import type { MessageStatistics, DateStatistic } from '~/types/calendar'
import type { SupportedLocale } from '~/types/localization'

export function useCalendarLogic() {
    /**
     * Generate calendar grid for the given month
     * Returns array of dates (numbers or null for padding)
     */
    const getMonthDays = (date: Date): (number | null)[] => {
        const year = date.getFullYear()
        const month = date.getMonth()

        // First day of month and total days
        const firstDay = new Date(year, month, 1).getDay()
        const daysInMonth = new Date(year, month + 1, 0).getDate()

        // Create grid with padding
        const grid: (number | null)[] = []

        // Add padding for days before month starts
        for (let i = 0; i < firstDay; i++) {
            grid.push(null)
        }

        // Add days of the month
        for (let day = 1; day <= daysInMonth; day++) {
            grid.push(day)
        }

        return grid
    }

    /**
     * Get maximum message count from statistics for heatmap scaling
     */
    const getMaxMessageCount = (stats: MessageStatistics): number => {
        if (stats.statistics.length === 0) return 1
        return Math.max(...stats.statistics.map(s => s.messageCount))
    }

    /**
     * Calculate heatmap color based on message count ratio
     * Returns CSS color string with opacity
     */
    const calculateHeatmapColor = (count: number, max: number): string => {
        if (count === 0) return 'transparent'
        if (max === 0) return 'transparent'

        const ratio = count / max
        const opacity = Math.ceil(ratio * 4) * 0.25 // 0.25, 0.5, 0.75, or 1.0

        // Bootstrap blue: rgb(0, 123, 255)
        return `rgba(0, 123, 255, ${opacity})`
    }

    /**
     * Get day of week labels for current locale
     */
    const getDayOfWeekLabels = (locale: SupportedLocale | 'auto'): string[] => {
        const resolvedLocale = locale === 'auto' ? getSystemLocale() : locale

        const formatter = new Intl.DateTimeFormat(resolvedLocale, {
            weekday: 'short',
        })

        // Create dates for each day of the week (starting Sunday)
        const labels: string[] = []
        const baseDate = new Date(2024, 0, 7) // A Sunday in Jan 2024

        for (let i = 0; i < 7; i++) {
            const date = new Date(baseDate)
            date.setDate(baseDate.getDate() + i)
            labels.push(formatter.format(date))
        }

        return labels
    }

    /**
     * Format month/year header for calendar
     */
    const formatMonthYear = (date: Date, locale: SupportedLocale | 'auto'): string => {
        const resolvedLocale = locale === 'auto' ? getSystemLocale() : locale

        return new Intl.DateTimeFormat(resolvedLocale, {
            month: 'long',
            year: 'numeric',
        }).format(date)
    }

    /**
     * Get message count for a specific date
     */
    const getMessageCountForDate = (
        date: number,
        stats: MessageStatistics | null,
    ): number => {
        if (!stats) return 0

        const dateStr = `${stats.year}-${String(stats.month).padStart(2, '0')}-${String(date).padStart(2, '0')}`
        const stat = stats.statistics.find(s => s.date === dateStr)
        return stat?.messageCount ?? 0
    }

    /**
     * Check if a date has messages
     */
    const hasMessages = (date: number, stats: MessageStatistics | null): boolean => {
        return getMessageCountForDate(date, stats) > 0
    }

    /**
     * Get system locale
     */
    const getSystemLocale = (): string => {
        return navigator.language || 'en-US'
    }

    return {
        getMonthDays,
        getMaxMessageCount,
        calculateHeatmapColor,
        getDayOfWeekLabels,
        formatMonthYear,
        getMessageCountForDate,
        hasMessages,
        getSystemLocale,
    }
}
