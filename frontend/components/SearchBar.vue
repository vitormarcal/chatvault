<template>
  <div class="search-bar d-flex gap-2">
    <input
        type="text"
        class="form-control search-input"
        :placeholder="t('searchPlaceholder')"
        v-model="store.searchQuery"
        @input="onSearch"
    />
    <button
        type="button"
        class="btn btn-outline-secondary calendar-btn"
        @click="toggleCalendar"
        :title="t('openCalendarTitle')"
        :aria-label="t('openCalendarAria')"
    >
      <svg class="calendar-icon" viewBox="0 0 24 24" aria-hidden="true" focusable="false">
        <path
          d="M7 2a1 1 0 0 1 1 1v1h8V3a1 1 0 1 1 2 0v1h1.5A2.5 2.5 0 0 1 22 6.5v12A2.5 2.5 0 0 1 19.5 21h-15A2.5 2.5 0 0 1 2 18.5v-12A2.5 2.5 0 0 1 4.5 4H6V3a1 1 0 0 1 1-1Zm12.5 8h-15a.5.5 0 0 0-.5.5v8a.5.5 0 0 0 .5.5h15a.5.5 0 0 0 .5-.5v-8a.5.5 0 0 0-.5-.5ZM4.5 6a.5.5 0 0 0-.5.5V8h16V6.5a.5.5 0 0 0-.5-.5h-15Z"
          fill="currentColor"
        />
      </svg>
    </button>
  </div>
</template>

<script setup lang="ts">
import { watch } from "vue";
import {useMainStore} from "~/store";
import { useUiText } from "~/composables/useUiText";
const store = useMainStore()
const { t } = useUiText()
const props = defineProps({
  chatId: { type: [Number, null], default: null },
});
const emit = defineEmits(["search"]);

function onSearch() {
  store.clearMessages();
  emit("search", { query: store.searchQuery || "", chatId: props.chatId });
}

function toggleCalendar() {
  if (store.calendarOpen) {
    store.closeCalendar();
  } else {
    store.openCalendar();
    if (props.chatId) {
      store.fetchMessageStatistics(Number(props.chatId), store.currentCalendarMonth);
    }
  }
}

watch(() => props.chatId, () => (store.searchQuery = ""));
</script>

<style scoped>
.search-bar {
  width: 100%;
  max-width: 380px;
  display: flex;
  gap: 0.5rem;
  align-items: stretch;
}

.search-bar input {
  flex: 1;
  min-width: 0;
}

.search-bar button {
  flex-shrink: 0;
  padding: 0.375rem 0.75rem;
  font-size: 1rem;
}

.search-input {
  border-radius: var(--radius-pill);
  border-color: var(--color-border-strong);
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 6px 16px rgba(15, 23, 42, 0.12);
}

.search-input::placeholder {
  color: #94a3b8;
}

.search-input:focus {
  border-color: var(--color-accent-strong);
  box-shadow: 0 0 0 0.2rem rgba(59, 130, 246, 0.2);
}

.calendar-btn {
  border-radius: var(--radius-pill);
  border-color: var(--color-border-strong);
  background: rgba(255, 255, 255, 0.92);
  width: 2.6rem;
  padding: 0;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 6px 16px rgba(15, 23, 42, 0.12);
}

.calendar-btn:hover:not(:disabled) {
  border-color: var(--color-accent-strong);
  background: rgba(255, 255, 255, 1);
}

.calendar-btn:focus-visible {
  outline: 2px solid var(--focus-ring);
  outline-offset: 2px;
}

.calendar-icon {
  width: 1.1rem;
  height: 1.1rem;
}
</style>
