<template>
  <div class="calendar-picker">
    <!-- Header with month/year and navigation -->
    <div class="calendar-header d-flex justify-content-between align-items-center mb-3">
      <button
        class="btn btn-sm btn-outline-secondary nav-btn"
        @click="previousMonth"
        :disabled="isLoading"
        :aria-label="t('previousMonth')"
      >
        ‹
      </button>
      <h5 class="month-label mb-0">{{ monthYearLabel }}</h5>
      <button
        class="btn btn-sm btn-outline-secondary nav-btn"
        @click="nextMonth"
        :disabled="isLoading"
        :aria-label="t('nextMonth')"
      >
        ›
      </button>
    </div>

    <!-- Day of week labels -->
    <div class="calendar-weekdays d-grid mb-2" style="grid-template-columns: repeat(7, 1fr); gap: 0.25rem;">
      <div
        v-for="(dayLabel, index) in dayLabels"
        :key="`day-${index}`"
        class="weekday text-center small text-muted"
      >
        {{ dayLabel }}
      </div>
    </div>

    <!-- Calendar grid -->
    <div class="calendar-grid d-grid" style="grid-template-columns: repeat(7, 1fr); gap: 0.25rem;">
      <button
        v-for="(day, index) in calendarDays"
        :key="`day-cell-${index}`"
        class="calendar-day"
        :class="{
          'has-messages': day !== null && hasMessages(day),
          'no-messages': day !== null && !hasMessages(day),
          'empty': day === null,
        }"
        :style="day !== null && hasMessages(day) ? {
          backgroundColor: heatmapColor(day),
        } : {}"
        :disabled="day === null || isLoading || !hasMessages(day)"
        @click="selectDate(day)"
      >
        <span v-if="day !== null" class="day-number">{{ day }}</span>
        <small v-if="day !== null && hasMessages(day)" class="message-count d-block">
          {{ getMessageCount(day) }}
        </small>
      </button>
    </div>

    <!-- Today button -->
    <div class="mt-3 text-center">
      <button
        class="btn btn-sm btn-primary today-btn"
        @click="goToToday"
        :disabled="isLoading"
      >
        {{ t('today') }}
      </button>
    </div>

    <!-- Loading state -->
    <div v-if="isLoading" class="mt-3 text-center">
      <div class="spinner-border spinner-border-sm text-primary" role="status">
        <span class="visually-hidden">{{ t('loading') }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import type { MessageStatistics } from '~/types/calendar'
import { useCalendarLogic } from '~/composables/useCalendarLogic'
import type { SupportedLocale } from '~/types/localization'
import { useUiText } from '~/composables/useUiText'

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
const { t } = useUiText()

const { getMonthDays, calculateHeatmapColor, getDayOfWeekLabels, formatMonthYear, getMessageCountForDate, hasMessages: hasMessagesLogic } = useCalendarLogic()

const localMonth = ref<Date>(new Date(props.currentMonth))

const calendarDays = computed(() => {
  return getMonthDays(localMonth.value)
})

const monthYearLabel = computed(() => {
  return formatMonthYear(localMonth.value, props.userLocale)
})

const dayLabels = computed(() => {
  return getDayOfWeekLabels(props.userLocale)
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

const selectDate = (day: number | null) => {
  if (day !== null && hasMessagesLogic(day, props.statistics)) {
    emit('select-date', day)
  }
}

const hasMessages = (day: number): boolean => {
  return hasMessagesLogic(day, props.statistics)
}

const getMessageCount = (day: number): number => {
  return getMessageCountForDate(day, props.statistics)
}

const heatmapColor = (day: number): string => {
  const count = getMessageCount(day)
  const max = props.statistics ? Math.max(...props.statistics.statistics.map(s => s.messageCount)) : 1
  return calculateHeatmapColor(count, max)
}

watch(() => props.currentMonth, (newMonth) => {
  localMonth.value = new Date(newMonth)
})
</script>

<style scoped>
.calendar-picker {
  padding: 1rem;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.98), rgba(248, 250, 252, 0.92));
  border-radius: 0.75rem;
}

.calendar-header {
  border-bottom: 1px solid var(--color-border-soft);
  padding-bottom: 0.75rem;
}

.month-label {
  font-weight: 600;
  letter-spacing: -0.01em;
  color: var(--color-text-dark);
}

.nav-btn {
  border-radius: var(--radius-pill);
  width: 2.1rem;
  height: 2.1rem;
  padding: 0;
  border-color: var(--color-border-strong);
  color: var(--color-text-dark);
  background: rgba(255, 255, 255, 0.7);
}

.nav-btn:hover:not(:disabled) {
  background: rgba(15, 23, 42, 0.04);
}

.weekday {
  padding: 0.4rem 0;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  font-size: 0.65rem;
}

.calendar-day {
  aspect-ratio: 1;
  border: 1px solid var(--color-border-soft);
  border-radius: 0.6rem;
  padding: 0.25rem;
  font-size: 0.875rem;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background-color: rgba(255, 255, 255, 0.95);
  color: var(--color-text-dark);
}

.calendar-day.empty {
  background-color: #f8fafc;
  border: none;
  cursor: default;
}

.calendar-day.no-messages {
  background-color: #f3f4f6;
  color: #9ca3af;
  cursor: default;
}

.calendar-day.has-messages {
  border-color: var(--color-accent-strong);
  font-weight: 500;
}

.calendar-day.has-messages:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 10px 20px rgba(15, 23, 42, 0.12);
}

.calendar-day:disabled {
  opacity: 1;
  cursor: not-allowed;
}

.day-number {
  font-weight: 600;
}

.message-count {
  font-size: 0.75rem;
  opacity: 0.85;
}

.calendar-weekdays {
  border-bottom: 1px solid var(--color-border-soft);
}

.today-btn {
  border-radius: var(--radius-pill);
  padding: 0.35rem 0.9rem;
  box-shadow: 0 6px 18px rgba(13, 110, 253, 0.2);
}
</style>
