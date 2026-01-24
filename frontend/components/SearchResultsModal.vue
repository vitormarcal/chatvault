<template>
  <div v-if="store.searchOpen" class="modal d-block search-backdrop">
    <div class="modal-dialog modal-dialog-centered modal-lg">
      <div class="modal-content">
        <div class="modal-header">
          <div>
            <h5 class="modal-title">{{ t('searchResultsTitle') }}</h5>
            <p class="modal-subtitle mb-0">{{ t('searchResultsSubtitle') }}</p>
          </div>
          <button type="button" class="btn-close" :aria-label="t('close')" @click="store.closeSearch()"></button>
        </div>
        <div class="modal-body">
          <div v-if="store.loading" class="text-center py-4">
            <div class="spinner-border" role="status">
              <span class="visually-hidden">{{ t('loading') }}</span>
            </div>
            <div class="text-muted mt-2 small">{{ t('searchingMessages') }}</div>
          </div>
          <div v-else-if="store.searchResults.length === 0" class="text-center text-muted py-4">
            <p class="mb-1">{{ t('noResults') }}</p>
            <small>{{ t('noResultsHint') }}</small>
          </div>
          <div v-else>
            <div class="results-summary text-muted small">
              {{ t('resultsCount', { count: store.searchResults.length }) }}
            </div>
            <div class="search-results-list">
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
                    <p class="card-text text-muted content-preview">{{ result.content }}</p>
                  </div>
                  <div class="text-right ms-2">
                    <small class="text-muted d-block">{{ formattedDate(result.createdAt) }}</small>
                    <button
                      class="btn btn-sm btn-primary mt-2 jump-btn"
                      @click.stop="jumpToDate(result.createdAt)"
                    >
                      {{ t('jumpToDate') }}
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
  </div>
</template>

<script setup lang="ts">
import { useMainStore } from '~/store';
import { useDateFormatting } from '~/composables/useDateFormatting';
import { useUiText } from '~/composables/useUiText';

const store = useMainStore();
const { formatDateFull } = useDateFormatting();
const { t } = useUiText();

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

.search-backdrop {
  background:
    radial-gradient(circle at 18% 18%, rgba(255, 255, 255, 0.16), transparent 45%),
    rgba(15, 23, 42, 0.6);
  backdrop-filter: blur(2px);
}

.modal-dialog {
  max-width: 780px;
}

.modal-content {
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-border-soft);
  background: var(--color-surface);
  box-shadow: var(--shadow-md);
  overflow: hidden;
}

.modal-header {
  background: linear-gradient(180deg, rgba(248, 249, 251, 0.9), rgba(248, 249, 251, 0.6));
  border-bottom: 1px solid var(--color-border-soft);
  border-top-left-radius: var(--radius-lg);
  border-top-right-radius: var(--radius-lg);
  padding: 1.25rem;
}

.btn-close:focus-visible {
  outline: 2px solid var(--focus-ring);
  outline-offset: 2px;
  border-radius: var(--radius-pill);
}

.modal-subtitle {
  font-size: 0.9rem;
  color: var(--color-text-muted-dark);
}

.modal-body {
  display: flex;
  flex-direction: column;
  gap: 0.85rem;
  padding: 1.25rem;
}

.search-result-item {
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
  border: 1px solid var(--color-border-soft);
  border-radius: var(--radius-md);
  background: rgba(255, 255, 255, 0.95);
}

.search-result-item:hover {
  background-color: rgba(255, 255, 255, 1);
  border-color: var(--color-accent-strong);
  transform: translateY(-1px);
  box-shadow: 0 12px 24px rgba(15, 23, 42, 0.12);
}

.search-results-list {
  max-height: 440px;
  overflow-y: auto;
  padding-right: 0.25rem;
}

.results-summary {
  margin-top: -0.25rem;
  font-weight: 500;
}

.content-preview {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin-bottom: 0;
}

.jump-btn {
  border-radius: var(--radius-pill);
  padding: 0.35rem 0.85rem;
  box-shadow: 0 6px 18px rgba(13, 110, 253, 0.2);
}
</style>
