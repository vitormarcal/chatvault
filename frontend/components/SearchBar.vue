<template>
  <div class="search-bar d-flex gap-2">
    <input
        type="text"
        class="form-control"
        placeholder="Search..."
        v-model="store.searchQuery"
        @input="onSearch"
    />
    <button
        type="button"
        class="btn btn-outline-secondary"
        @click="toggleCalendar"
        title="Open calendar date picker"
        aria-label="Open calendar"
    >
      📅
    </button>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from "vue";
import {useMainStore} from "~/store";
const store = useMainStore()
const props = defineProps({
  chatId: { type: [Number, null], default: null },
});
const emit = defineEmits(["search"]);

const searchQuery = ref("");

function onSearch() {
  store.clearMessages();
  emit("search", { query: searchQuery.value, chatId: props.chatId });
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

watch(() => props.chatId, () => (searchQuery.value = ""));
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
</style>
