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
            <h5 class="modal-title">Jump to Date</h5>
            <button
              type="button"
              class="btn-close"
              @click="closeModal"
              aria-label="Close"
            />
          </div>

          <div class="modal-body">
            <!-- Loading state -->
            <div v-if="statisticsLoading" class="text-center py-5">
              <div class="spinner-border text-primary" role="status">
                <span class="visually-hidden">Loading calendar...</span>
              </div>
            </div>

            <!-- Error state -->
            <div v-else-if="!messageStatistics" class="alert alert-warning mb-0">
              Failed to load calendar data. Please try again.
            </div>

            <!-- Smart view: Calendar or List -->
            <template v-else>
              <!-- Calendar view for dense data -->
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
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  animation: fadeIn 0.2s ease-in-out;
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
  max-width: 500px;
  animation: slideUp 0.3s ease-out;
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
  background-color: white;
  background-clip: padding-box;
  border: 1px solid rgba(0, 0, 0, 0.15);
  border-radius: 0.375rem;
  outline: 0;
  box-shadow: 0 0.5rem 1rem rgba(0, 0, 0, 0.15);
}

.modal-header {
  padding: 1rem;
  border-bottom: 1px solid #dee2e6;
  border-top-left-radius: calc(0.375rem - 1px);
  border-top-right-radius: calc(0.375rem - 1px);
}

.modal-title {
  margin-bottom: 0;
  line-height: 1.5;
  font-size: 1.25rem;
  font-weight: 500;
}

.modal-body {
  position: relative;
  flex: 1 1 auto;
  padding: 1rem;
}

.btn-close {
  padding: 0.25rem;
  background: transparent url("data:image/svg+xml,%3csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 16 16' fill='%23000'%3e%3cpath d='M.293.293a1 1 0 0 1 1.414 0L8 6.586 14.293.293a1 1 0 1 1 1.414 1.414L9.414 8l6.293 6.293a1 1 0 0 1-1.414 1.414L8 9.414l-6.293 6.293a1 1 0 0 1-1.414-1.414L6.586 8 .293 1.707a1 1 0 0 1 0-1.414z'/%3e%3c/svg%3e") center / 1em auto no-repeat;
  border: 0;
  width: 1.5rem;
  height: 1.5rem;
  cursor: pointer;
  color: #000;
  opacity: 0.5;
}

.btn-close:hover {
  opacity: 0.75;
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
