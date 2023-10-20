<template>
  <div class="message-item rounded d-flex flex-column mt-3" :class="classObject">
    <div class="message-id">{{ message.id }}</div>
    <div class="author font-weight-bold">{{ message.author }}</div>
    <div class="message-content" v-html="safeContent"></div>
    <attachment v-if="hasAttachment" :message="message" :chatId="chatId"></attachment>
    <div class="message-createdAt">{{ message.createdAt }}</div>
  </div>
</template>

<script setup lang="ts">
import Attachment from "~/components/Attachment.vue";

const props = defineProps(["message", "chatId"])

const safeContent = computed(() => props.message.content.replace(
    /https?:\/\/[^\s]+/g,
    '<a href="$&" target="_blank">$&</a>'
))

const hasAttachment = computed(() => !!props.message.attachmentName)
const isSystem = computed(() => props.message.authorType === 'SYSTEM')

const classObject = computed(() => {
  return {
    'system-message': isSystem.value,
  }
})
</script>


<style scoped>

.message-item {
  padding: 8px 10px;
  box-shadow: rgba(0, 0, 0, 0.2) 0 1px 1px;
  text-align: left;
  position: relative;
}

.message-id {
  color: rgb(241, 241, 242);
  background-color: rgb(14, 97, 98);

  position: absolute;

  font-size: 10px;
  padding-inline: 7px;
  border-radius: 99px;
  border: 1px solid rgba(0, 0, 0, 0.15);
  top: -0.5em;
  right: -0.5em;
  opacity: 0;
  transition: opacity 0.3s ease 0s;
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
  flex: 0 0 auto;
  align-self: flex-end;
}

</style>