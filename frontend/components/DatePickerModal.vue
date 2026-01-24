<template>
  <teleport to="body">
    <div
      v-if="isOpen"
      class="modal-backdrop"
      @click.self="closeModal"
    >
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header d-flex justify-content-between align-items-center">
            <div>
              <h5 class="modal-title">{{ t('jumpToDateTitle') }}</h5>
              <p class="modal-subtitle mb-0">{{ t('jumpToDateSubtitle') }}</p>
            </div>
            <button
              type="button"
              class="btn-close"
              @click="closeModal"
              :aria-label="t('close')"
            />
          </div>

          <div class="modal-body">
            <!-- Loading state -->
            <div v-if="statisticsLoading" class="text-center py-5">
              <div class="spinner-border text-primary" role="status">
                <span class="visually-hidden">{{ t('loading') }}</span>
              </div>
              <div class="text-muted mt-2 small">{{ t('preparingTimeline') }}</div>
            </div>

            <!-- Error state -->
            <div v-else-if="!messageStatistics" class="alert alert-warning mb-0">
              {{ t('calendarLoadError') }}
            </div>

            <!-- Smart view: Calendar or List -->
            <template v-else>
              <!-- Calendar view for dense data -->
              <div class="picker-surface">
                <date-picker-calendar
                  v-if="messageStatistics.isDataDense"
                  :statistics="messageStatistics"
                  :current-month="currentCalendarMonth"
                  :is-loading="statisticsLoading"
                  :user-locale="userLocale"
                  @select-date="handleDateSelect"
                  @month-changed="handleMonthChange"
                />

              <!-- List view for sparse data -->
                <date-picker-list
                  v-else
                  :statistics="messageStatistics"
                  :current-month="currentCalendarMonth"
                  :is-loading="statisticsLoading"
                  :user-locale="userLocale"
                  @select-date="handleDateSelect"
                  @month-changed="handleMonthChange"
                />
              </div>
              <div class="modal-hint text-muted small">
                {{ t('monthNavTip') }}
              </div>
            </template>
          </div>
        </div>
      </div>
    </div>
  </teleport>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { MessageStatistics } from '~/types/calendar'
import type { SupportedLocale } from '~/types/localization'
import { useUiText } from '~/composables/useUiText'

const props = defineProps<{
  isOpen: boolean
  messageStatistics: MessageStatistics | null
  currentCalendarMonth: Date
  statisticsLoading: boolean
  userLocale: SupportedLocale | 'auto'
}>()

const emit = defineEmits<{
  close: []
  'select-date': [date: string]
  'month-changed': [date: Date]
}>()
const { t } = useUiText()

const closeModal = () => {
  emit('close')
}

const handleDateSelect = (day: number) => {
  if (!props.messageStatistics) return

  const dateStr = `${props.messageStatistics.year}-${String(props.messageStatistics.month).padStart(2, '0')}-${String(day).padStart(2, '0')}`
  emit('select-date', dateStr)
  closeModal()
}

const handleMonthChange = (date: Date) => {
  emit('month-changed', date)
}
</script>

<style scoped>
.modal-backdrop {
  position: fixed;
  top: 0;
  left: 0;
  z-index: 1050;
  width: 100%;
  height: 100%;
  background:
    radial-gradient(circle at 20% 20%, rgba(255, 255, 255, 0.18), transparent 45%),
    rgba(15, 23, 42, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  animation: fadeIn 0.2s ease-in-out;
  backdrop-filter: blur(2px);
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.modal-dialog {
  position: relative;
  width: auto;
  margin: 1rem;
  pointer-events: none;
  max-width: 620px;
  animation: slideUp 0.35s ease-out;
}

@keyframes slideUp {
  from {
    transform: translateY(50px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

.modal-content {
  position: relative;
  display: flex;
  flex-direction: column;
  width: 100%;
  pointer-events: auto;
  background-color: var(--color-surface);
  background-clip: padding-box;
  border: 1px solid var(--color-border-soft);
  border-radius: var(--radius-lg);
  overflow: hidden;
  outline: 0;
  box-shadow: var(--shadow-md);
}

.modal-header {
  padding: 1.25rem 1.25rem 1rem;
  border-bottom: 1px solid var(--color-border-soft);
  border-top-left-radius: var(--radius-lg);
  border-top-right-radius: var(--radius-lg);
  background: linear-gradient(180deg, rgba(248, 249, 251, 0.9), rgba(248, 249, 251, 0.6));
}

.modal-title {
  margin-bottom: 0;
  line-height: 1.5;
  font-size: 1.35rem;
  font-weight: 600;
  letter-spacing: -0.01em;
}

.modal-subtitle {
  font-size: 0.92rem;
  color: var(--color-text-muted-dark);
}

.modal-body {
  position: relative;
  flex: 1 1 auto;
  padding: 1.25rem;
  display: flex;
  flex-direction: column;
  gap: 0.9rem;
}

.btn-close {
  padding: 0.25rem;
  background: transparent url("data:image/svg+xml,%3csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 16 16' fill='%23000'%3e%3cpath d='M.293.293a1 1 0 0 1 1.414 0L8 6.586 14.293.293a1 1 0 1 1 1.414 1.414L9.414 8l6.293 6.293a1 1 0 0 1-1.414 1.414L8 9.414l-6.293 6.293a1 1 0 0 1-1.414-1.414L6.586 8 .293 1.707a1 1 0 0 1 0-1.414z'/%3e%3c/svg%3e") center / 1em auto no-repeat;
  border: 0;
  width: 1.5rem;
  height: 1.5rem;
  cursor: pointer;
  color: #000;
  opacity: 0.55;
  border-radius: 999px;
}

.btn-close:hover {
  opacity: 0.8;
  background-color: rgba(15, 23, 42, 0.06);
}

.btn-close:focus-visible {
  outline: 2px solid var(--focus-ring);
  outline-offset: 2px;
  border-radius: var(--radius-pill);
}

.picker-surface {
  background: #ffffff;
  border: 1px solid var(--color-border-soft);
  border-radius: 14px;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.06);
}

.picker-surface :deep(.calendar-picker),
.picker-surface :deep(.date-picker-list) {
  background: transparent;
  padding: 1.1rem;
}

.picker-surface :deep(.calendar-header),
.picker-surface :deep(.list-header),
.picker-surface :deep(.calendar-weekdays) {
  border-bottom: 1px solid var(--color-border-soft);
}

.picker-surface :deep(.calendar-day) {
  border-color: var(--color-border-soft);
  border-radius: 10px;
}

.picker-surface :deep(.calendar-day.has-messages:hover:not(:disabled)) {
  transform: translateY(-1px);
  box-shadow: 0 6px 14px rgba(15, 23, 42, 0.12);
}

.picker-surface :deep(.list-item) {
  border-radius: 10px;
  border-color: var(--color-border-soft);
}

.picker-surface :deep(.list-item:hover:not(:disabled)) {
  border-color: var(--color-accent-strong);
  background-color: rgba(13, 110, 253, 0.06);
}

.modal-hint {
  border-top: 1px solid var(--color-border-soft);
  padding-top: 0.65rem;
}

@media (max-width: 576px) {
  .modal-dialog {
    margin: 0.5rem;
    max-width: calc(100% - 1rem);
  }

  .modal-body {
    max-height: calc(100vh - 200px);
    overflow-y: auto;
  }
}
</style>
