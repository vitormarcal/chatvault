<template>
  <div class="attachment">
    <img class="w-100 img-thumbnail" v-if="isImage" :src="url" :alt="message.attachmentName"/>
    <video class="w-100" v-else-if="isVideo" :src="url" controls></video>
    <audio class="w-100" v-else-if="isAudio" :src="url" controls></audio>
    <a class="w-100" v-else :download='message.attachmentName'
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