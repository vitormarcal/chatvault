<template>
  <div v-if="url" class="attachment">
    <a :download='message.attachmentName'
       :href="url">{{ message.attachmentName }}</a>
  </div>
</template>

<script setup lang="ts">
const getAttachmentApiUrl = useRuntimeConfig().public.api.getAttachmentByChatIdAndMessageId
const props = defineProps(['message', 'chatId'])
const url = computed(() =>  getAttachmentApiUrl
    .replace(':chatId', props.chatId)
    .replace(':messageId', props.message.id))
</script>
<style scoped>

</style>