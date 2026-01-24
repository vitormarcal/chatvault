<template>
  <div class="date-picker-list">
    <!-- Header with month/year and navigation -->
    <div class="list-header d-flex justify-content-between align-items-center mb-3">
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

    <!-- Date list -->
    <div class="dates-list">
      <div v-if="datesWithMessages.length === 0" class="empty-state mb-0">
        <div class="empty-title">{{ t('noMessagesMonth') }}</div>
        <small class="text-muted">{{ t('noMessagesHint') }}</small>
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
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.98), rgba(248, 250, 252, 0.92));
  border-radius: 0.75rem;
}

.list-header {
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
  padding: 0.8rem 0.9rem;
  border-radius: 0.7rem;
  border: 1px solid var(--color-border-soft);
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
  background: rgba(255, 255, 255, 0.9);
}

.list-item:hover:not(:disabled) {
  border-color: var(--color-accent-strong);
  background-color: rgba(13, 110, 253, 0.06);
  transform: translateY(-1px);
  box-shadow: 0 10px 20px rgba(15, 23, 42, 0.12);
}

.list-item:active {
  transform: translateY(0);
}

.list-item:disabled {
  opacity: 0.6;
}

.list-item strong {
  display: block;
  color: var(--color-text-dark);
  margin-bottom: 0.2rem;
}

.list-item small {
  font-size: 0.75rem;
}

.list-item .badge {
  border-radius: var(--radius-pill);
  padding: 0.35rem 0.55rem;
}

.empty-state {
  border: 1px dashed var(--color-border-strong);
  border-radius: 0.75rem;
  padding: 1.25rem;
  text-align: center;
  background: rgba(248, 250, 252, 0.8);
}

.empty-title {
  font-weight: 600;
  color: var(--color-text-dark);
  margin-bottom: 0.2rem;
}

.today-btn {
  border-radius: var(--radius-pill);
  padding: 0.35rem 0.9rem;
  box-shadow: 0 6px 18px rgba(13, 110, 253, 0.2);
}
</style>
