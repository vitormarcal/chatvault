<template>
  <div
      class="message-item rounded d-flex flex-column mt-3"
      :class="messageClasses"
  >
    <div class="message-id">{{ message.id }}</div>
    <div class="author font-weight-bold">{{ message.author }}</div>
    <div
        class="message-content"
        v-html="formattedContent"
        :class="{ 'blur-sensitive': store.blurEnabled }"
    ></div>
    <focusable-attachment
        v-if="hasAttachment"
        :attachment="message.attachment"
        :class="{ 'blur-sensitive': store.blurEnabled }"
    />
    <div class="message-createdAt">{{ message.createdAt }}</div>
  </div>
</template>

<script setup lang="ts">
import { useMainStore } from '~/store';

const store = useMainStore();

const props = defineProps(['message']);

const formattedContent = computed(() =>
    props.message.content.replace(
        /https?:\/\/[^\s]+/g,
        '<a href="$&" target="_blank" rel="noopener noreferrer">$&</a>'
    )
);

const hasAttachment = computed(() => Boolean(props.message.attachment));

const isSystemMessage = computed(() => props.message.authorType === 'SYSTEM');

const isAuthorSelf = computed(() => props.message.author === store.authorActive);

const messageClasses = computed(() => ({
  'system-message w-50 align-self-center': isSystemMessage.value,
  'align-self-end': isAuthorSelf.value,
  'align-self-start': !isAuthorSelf.value,
}));
</script>

<style scoped>
.message-item {
  padding: 8px 10px;
  box-shadow: 0 1px 1px rgba(0, 0, 0, 0.2);
  text-align: left;
  position: relative;
  background: #000000;
}

.message-id {
  color: rgb(241, 241, 242);
  background-color: rgb(14, 97, 98);
  position: absolute;
  font-size: 10px;
  padding-inline: 7px;
  border-radius: 50%;
  border: 1px solid rgba(0, 0, 0, 0.15);
  top: -0.5em;
  right: -0.5em;
  opacity: 0;
  transition: opacity 0.3s ease-in-out;
}

.message-item:hover .message-id {
  opacity: 1;
}

.message-content {
  overflow-wrap: break-word;
  word-break: break-word;
  white-space: pre-wrap;
}

.system-message .message-content {
  text-align: center;
}

.message-createdAt {
  margin-left: 1rem;
  white-space: nowrap;
  font-size: 75%;
  opacity: 0.6;
  align-self: flex-end;
}

.blur-sensitive {
  filter: blur(6px);
  transition: filter 0.3s ease-in-out;
}

.message-item:hover .blur-sensitive {
  filter: none;
}
</style>
