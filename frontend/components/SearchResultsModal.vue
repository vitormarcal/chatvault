<template>
  <div v-if="store.searchOpen" class="modal d-block" style="background: rgba(0, 0, 0, 0.5);">
    <div class="modal-dialog modal-dialog-centered modal-lg">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title">Search Results</h5>
          <button type="button" class="btn-close" @click="store.closeSearch()"></button>
        </div>
        <div class="modal-body">
          <div v-if="store.loading" class="text-center">
            <div class="spinner-border" role="status">
              <span class="visually-hidden">Loading...</span>
            </div>
          </div>
          <div v-else-if="store.searchResults.length === 0" class="text-center text-muted">
            <p>No results found</p>
          </div>
          <div v-else class="search-results-list">
            <div
              v-for="result in store.searchResults"
              :key="`${result.id}`"
              class="search-result-item card mb-2"
              role="button"
              @click="jumpToDate(result.createdAt)"
            >
              <div class="card-body">
                <div class="d-flex justify-content-between align-items-start">
                  <div class="flex-grow-1">
                    <h6 class="card-title mb-1">{{ result.author }}</h6>
                    <p class="card-text text-truncate text-muted">{{ result.content }}</p>
                  </div>
                  <div class="text-right ms-2">
                    <small class="text-muted d-block">{{ formattedDate(result.createdAt) }}</small>
                    <button
                      class="btn btn-sm btn-primary mt-2"
                      @click.stop="jumpToDate(result.createdAt)"
                    >
                      Jump to date
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useMainStore } from '~/store';
import { useDateFormatting } from '~/composables/useDateFormatting';

const store = useMainStore();
const { formatDateFull } = useDateFormatting();

function formattedDate(dateStr: string): string {
  try {
    const date = new Date(dateStr);
    return formatDateFull(date);
  } catch {
    return dateStr;
  }
}

function jumpToDate(targetDate: string) {
  if (!store.chatActive.chatId) return;
  store.jumpToDate(store.chatActive.chatId, targetDate);
}
</script>

<style scoped>
.modal.d-block {
  display: block !important;
}

.search-result-item {
  cursor: pointer;
  transition: background-color 0.2s ease;
  border: 1px solid #dee2e6;
}

.search-result-item:hover {
  background-color: #f8f9fa;
  border-color: #007bff;
}

.search-results-list {
  max-height: 400px;
  overflow-y: auto;
}
</style>
