<template>
  <div class="chat-item d-flex flex-row p-2 w-100" @click="emitThisChatActive()">
    <profile-image :id="item.chatId"/>
    <div
        class="w-50"
        :class="{ 'blur-sensitive': store.blurEnabled }"
        style="padding-left: .5rem"
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
  background: #000000;
  border-bottom: 1px solid #360d3c;
}

.chat-item:hover {
  background: #360d3c;
}

.blur-sensitive {
  filter: blur(6px);
  transition: filter 0.3s ease-in-out;
}

.chat-item:hover .blur-sensitive {
  filter: none;
}
</style>
