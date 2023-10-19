<template>
  <div class="message-item rounded d-flex flex-column">
    <div class="message-id">{{ message.id }}</div>
    <div class="author font-weight-bold">{{ message.author }}</div>
    <div class="message-content" v-html="safeContent"></div>
    <attachment :message="message"></attachment>
    <div class="message-createdAt">{{ message.createdAt }}</div>
  </div>
</template>

<script setup lang="ts">
import Attachment from "~/components/Attachment.vue";

const props = defineProps(["message"])

const safeContent = computed(() => props.message.content.replace(
    /https?:\/\/[^\s]+/g,
    '<a href="$&" target="_blank">$&</a>'
))
</script>


<style scoped>

</style>