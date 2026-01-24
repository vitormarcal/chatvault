<template>
  <div class="attachment">
    <img class="attachment-media img-thumbnail" loading="lazy" v-if="isImage" :src="attachment.url" :alt="attachment.name"/>
    <video class="attachment-media" v-else-if="isVideo" :src="attachment.url" controls></video>
    <audio class="attachment-media" v-else-if="isAudio" :src="attachment.url" controls></audio>
    <a class="attachment-link" v-else :download='attachment.name'
       :href="attachment.url">
      <span class="attachment-file">{{ attachment.name }}</span>
      <span class="attachment-download">{{ t('download') }}</span>
    </a>
    <slot></slot>
  </div>
</template>

<script setup lang="ts">
import { useUiText } from '~/composables/useUiText'

const props = defineProps(['attachment'])
const { t } = useUiText()

const isImage = computed(() => props.attachment.type === 'IMAGE')
const isVideo = computed(() => props.attachment.type === 'VIDEO')
const isAudio = computed(() => props.attachment.type === 'AUDIO')
const isPDF = computed(() => props.attachment.type === 'PDF')
</script>
<style scoped>
.attachment {
  border-radius: var(--radius-md);
  border: 1px solid var(--color-border);
  background: rgba(15, 23, 42, 0.15);
  padding: 0.5rem;
  box-shadow: var(--shadow-sm);
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.attachment:hover {
  border-color: var(--color-accent-strong);
  box-shadow: 0 16px 30px rgba(2, 6, 23, 0.35);
}

.attachment:focus-within {
  border-color: var(--color-accent-strong);
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.25);
}

.attachment-media {
  width: 100%;
  border-radius: var(--radius-sm);
  border: none;
  background: var(--color-bg-elevated);
}

.attachment-link {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
  text-decoration: none;
  color: var(--color-text);
  padding: 0.75rem;
  border-radius: var(--radius-sm);
  border: 1px solid var(--color-border);
  background: rgba(15, 23, 42, 0.3);
  transition: transform 0.2s ease, border-color 0.2s ease, background-color 0.2s ease;
}

.attachment-link:hover {
  border-color: var(--color-accent-strong);
  background: var(--color-accent-soft);
  transform: translateY(-1px);
}

.attachment-link:focus-visible {
  outline: 2px solid var(--focus-ring);
  outline-offset: 2px;
}

.attachment-file {
  font-weight: 600;
  word-break: break-word;
}

.attachment-download {
  font-size: 0.8rem;
  color: var(--color-text-muted);
  text-transform: uppercase;
  letter-spacing: 0.08em;
}
</style>
