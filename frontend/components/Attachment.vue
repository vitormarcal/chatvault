<template>
  <div class="attachment">
    <img v-if="isImage" :src="url" :alt="message.attachmentName"/>
    <video v-else-if="isVideo" :src="url" controls></video>
    <audio v-else-if="isAudio" :src="url" controls></audio>
    <video v-else-if="isPDF" :src="url" controls></video>
    <a v-else :download='message.attachmentName'
       :href="url">{{ message.attachmentName }}</a>
  </div>
</template>

<script setup lang="ts">
const getAttachmentApiUrl = useRuntimeConfig().public.api.getAttachmentByChatIdAndMessageId
const props = defineProps(['message', 'chatId'])

const isImage = computed(() => /\.(jpg|jpeg|png|gif|webp)$/i.test(props.message.attachmentName))
const isVideo = computed(() => /\.(mp4|avi|mov)$/i.test(props.message.attachmentName))
const isAudio = computed(() => /\.(mp3|wav|opus)$/i.test(props.message.attachmentName))
const isPDF = computed(() => /\.(pdf)$/i.test(props.message.attachmentName))

const url = computed(() =>  getAttachmentApiUrl
    .replace(':chatId', props.chatId)
    .replace(':messageId', props.message.id))
</script>
<style scoped>

</style>