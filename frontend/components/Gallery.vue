<script setup lang="ts">
import { useMainStore } from "~/store";

const store = useMainStore();

const galleryOptions = ref([
  { type: "ALL", label: "All" },
  { type: "VIDEO", label: "Video Files" },
  { type: "IMAGE", label: "Image Files" },
  { type: "PDF", label: "Documents" },
  { type: "AUDIO", label: "Audio Files" },
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
  <div>
    <slot></slot>
    <div class="title">Gallery</div>
    <ul class="nav justify-content-end">
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

    <div class="row">
      <div
          class="col-md-4"
          v-for="item in attachments"
          :key="item.url"
          v-memo="galleryFileType"
      >
        <div class="card">
          <focusable-attachment :attachment="item" />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.nav-link.active {
  font-weight: bold;
}
</style>
