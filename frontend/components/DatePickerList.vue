<template>
  <div class="date-picker-list">
    <!-- Header with month/year and navigation -->
    <div class="list-header d-flex justify-content-between align-items-center mb-3">
      <button
        class="btn btn-sm btn-outline-secondary"
        @click="previousMonth"
        :disabled="isLoading"
        aria-label="Previous month"
      >
        ‹
      </button>
      <h5 class="mb-0">{{ monthYearLabel }}</h5>
      <button
        class="btn btn-sm btn-outline-secondary"
        @click="nextMonth"
        :disabled="isLoading"
        aria-label="Next month"
      >
        ›
      </button>
    </div>

    <!-- Date list -->
    <div class="dates-list">
      <div v-if="datesWithMessages.length === 0" class="alert alert-info mb-0">
        No messages this month
      </div>
      <button
        v-for="item in datesWithMessages"
        :key="item.dateStr"
        class="list-item btn btn-outline-primary d-flex justify-content-between align-items-center"
        @click="selectDate(item.day)"
        :disabled="isLoading"
      >
        <span>
          <strong>{{ item.formattedDate }}</strong>
          <small class="text-muted d-block">{{ item.dayOfWeek }}</small>
        </span>
        <span class="badge bg-primary">{{ item.messageCount }}</span>
      </button>
    </div>

    <!-- Today button -->
    <div class="mt-3 text-center">
      <button
        class="btn btn-sm btn-primary"
        @click="goToToday"
        :disabled="isLoading"
      >
        Today
      </button>
    </div>

    <!-- Loading state -->
    <div v-if="isLoading" class="mt-3 text-center">
      <div class="spinner-border spinner-border-sm text-primary" role="status">
        <span class="visually-hidden">Loading...</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import type { MessageStatistics } from '~/types/calendar'
import { useCalendarLogic } from '~/composables/useCalendarLogic'
import type { SupportedLocale } from '~/types/localization'

const props = defineProps<{
  statistics: MessageStatistics | null
  currentMonth: Date
  isLoading: boolean
  userLocale: SupportedLocale | 'auto'
}>()

const emit = defineEmits<{
  'select-date': [day: number]
  'month-changed': [date: Date]
}>()

const { formatMonthYear } = useCalendarLogic()

const localMonth = ref<Date>(new Date(props.currentMonth))

const monthYearLabel = computed(() => {
  return formatMonthYear(localMonth.value, props.userLocale)
})

const datesWithMessages = computed(() => {
  if (!props.statistics) return []

  return props.statistics.statistics.map(stat => {
    const [year, month, day] = stat.date.split('-').map(Number)
    const date = new Date(year, month - 1, day)

    const formatter = new Intl.DateTimeFormat(props.userLocale === 'auto' ? navigator.language : props.userLocale, {
      weekday: 'long',
      month: 'long',
      day: 'numeric',
      year: 'numeric',
    })

    const dayOfWeekFormatter = new Intl.DateTimeFormat(props.userLocale === 'auto' ? navigator.language : props.userLocale, {
      weekday: 'long',
    })

    return {
      day,
      dateStr: stat.date,
      formattedDate: formatter.format(date),
      dayOfWeek: dayOfWeekFormatter.format(date),
      messageCount: stat.messageCount,
    }
  })
})

const previousMonth = () => {
  const newDate = new Date(localMonth.value)
  newDate.setMonth(newDate.getMonth() - 1)
  localMonth.value = newDate
  emit('month-changed', newDate)
}

const nextMonth = () => {
  const newDate = new Date(localMonth.value)
  newDate.setMonth(newDate.getMonth() + 1)
  localMonth.value = newDate
  emit('month-changed', newDate)
}

const goToToday = () => {
  const today = new Date()
  localMonth.value = new Date(today)
  emit('month-changed', new Date(today))
}

const selectDate = (day: number) => {
  emit('select-date', day)
}

watch(() => props.currentMonth, (newMonth) => {
  localMonth.value = new Date(newMonth)
})
</script>

<style scoped>
.date-picker-list {
  padding: 1rem;
  background: white;
  border-radius: 0.375rem;
}

.list-header {
  border-bottom: 1px solid #dee2e6;
  padding-bottom: 0.75rem;
}

.dates-list {
  max-height: 400px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.list-item {
  width: 100%;
  text-align: left;
  padding: 0.75rem;
  border-radius: 0.375rem;
  border: 1px solid #dee2e6;
  transition: all 0.2s ease;
}

.list-item:hover:not(:disabled) {
  border-color: #0d6efd;
  background-color: #f0f6ff;
}

.list-item:active {
  transform: scale(0.98);
}

.list-item:disabled {
  opacity: 0.6;
}

.list-item strong {
  display: block;
  color: #212529;
  margin-bottom: 0.25rem;
}

.list-item small {
  font-size: 0.75rem;
}
</style>
