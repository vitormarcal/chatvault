import type { MessageStatistics } from '~/types/calendar'

export function useCalendarApi() {
    const fetchStatistics = async (
        chatId: number,
        date: Date,
    ): Promise<MessageStatistics | null> => {
        try {
            const year = date.getFullYear()
            const month = date.getMonth() + 1
            const url = useRuntimeConfig().public.api.getMessageStatistics
                .replace(':chatId', chatId.toString())
                .replace(':year', year.toString())
                .replace(':month', month.toString())
            return await $fetch<MessageStatistics>(url)
        } catch (error) {
            console.error('Failed to fetch message statistics:', error)
            return null
        }
    }

    return {
        fetchStatistics,
    }
}
