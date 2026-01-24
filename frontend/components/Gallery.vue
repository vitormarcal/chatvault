<script setup lang="ts">
import { useMainStore } from "~/store";
import { useUiText } from "~/composables/useUiText";

const store = useMainStore();

const { t } = useUiText();

const galleryOptions = computed(() => [
  { type: "ALL", label: t('filterAll') },
  { type: "VIDEO", label: t('filterVideo') },
  { type: "IMAGE", label: t('filterImage') },
  { type: "PDF", label: t('filterDocuments') },
  { type: "AUDIO", label: t('filterAudio') },
]);

const galleryFileType = ref("ALL");

const attachments = computed(() =>
    galleryFileType.value === "ALL"
        ? store.attachments
        : store.attachments.filter((item) => item.type === galleryFileType.value)
);

function setGalleryFilter(type: string) {
  galleryFileType.value = type;
}
</script>

<template>
  <div class="gallery-panel">
    <slot></slot>
    <div class="gallery-header">
    <div class="title">{{ t('galleryTitle') }}</div>
      <ul class="nav filter-tabs justify-content-end">
      <li
          v-for="item in galleryOptions"
          :key="item.type"
          class="nav-item"
      >
        <button
            class="nav-link active"
            :class="{ active: galleryFileType === item.type }"
            @click="setGalleryFilter(item.type)"
        >
          {{ item.label }}
        </button>
      </li>
      </ul>
    </div>

    <div class="row gallery-grid">
      <div
          class="col-md-4"
          v-for="item in attachments"
          :key="item.url"
          v-memo="galleryFileType"
      >
        <div class="card gallery-card">
          <focusable-attachment :attachment="item" />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.gallery-panel {
  color: var(--color-text);
}

.gallery-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.75rem;
  margin-bottom: 0.75rem;
}

.title {
  font-size: 1.1rem;
  font-weight: 600;
  letter-spacing: -0.01em;
}

.filter-tabs {
  gap: 0.4rem;
  flex-wrap: wrap;
}

.filter-tabs .nav-link {
  border-radius: var(--radius-pill);
  border: 1px solid var(--color-border-strong);
  color: var(--color-text);
  padding: 0.25rem 0.7rem;
  background: rgba(15, 23, 42, 0.2);
  font-size: 0.85rem;
}

.filter-tabs .nav-link.active {
  background: rgba(59, 130, 246, 0.2);
  border-color: var(--color-accent-strong);
  font-weight: 600;
}

.gallery-grid {
  margin-top: 0.5rem;
  row-gap: 0.75rem;
}

.gallery-card {
  border-radius: var(--radius-md);
  border: 1px solid var(--color-border);
  background: rgba(15, 23, 42, 0.3);
  box-shadow: var(--shadow-sm);
}
</style>
