export interface DateStatistic {
    date: string // ISO date string YYYY-MM-DD
    messageCount: number
}

export interface MessageStatistics {
    month: number
    year: number
    statistics: DateStatistic[]
    minDate: string // ISO date
    maxDate: string // ISO date
    isDataDense: boolean
}
