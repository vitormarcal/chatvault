<template>
  <div class="chat-item d-flex flex-row p-2 w-100" @click="emitThisChatActive()">
    <profile-image :id="item.chatId" :class="{ 'blur-sensitive': store.blurEnabled }" />
    <div
        class="w-50 chat-preview"
        :class="{ 'blur-sensitive': store.blurEnabled }"
    >
      <div class="name">{{ item.chatName }}</div>
      <div class="small text-truncate">
        <i class="far fa-check-circle mr-1"></i>
        {{ item.content }}
      </div>
    </div>
    <message-created-at :date="item.msgCreatedAt" />
  </div>
</template>

<script setup lang="ts">
import { useMainStore } from "~/store";

const store = useMainStore();
const props = defineProps(["item"]);
const emit = defineEmits(["update:chat-active"]);

function emitThisChatActive() {
  emit("update:chat-active", props.item);
}
</script>

<style>
.chat-item {
  cursor: pointer;
  background: rgba(2, 6, 23, 0.95);
  border-bottom: 1px solid var(--color-border);
  transition: background-color 0.2s ease, transform 0.2s ease;
  align-items: center;
}

.chat-item:hover {
  background: var(--color-accent-soft);
  transform: translateY(-1px);
}

.chat-preview {
  padding-left: 0.6rem;
}

.name {
  color: var(--color-text);
  font-weight: 600;
  letter-spacing: -0.01em;
}

.chat-item .small {
  color: var(--color-text-muted);
}

.blur-sensitive {
  filter: blur(6px);
  transition: filter 0.3s ease-in-out;
}

.chat-item:hover .blur-sensitive {
  filter: none;
}
</style>
