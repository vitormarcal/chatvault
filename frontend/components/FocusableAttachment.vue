<script setup lang="ts">
import { useUiText } from '~/composables/useUiText'

const props = defineProps(['attachment'])
const { t } = useUiText()

let showMedia = ref(false);
const modalClass = computed(() => {
  return {
    'fade show d-block': !!showMedia.value
  }
})

function toggleModal() {
  showMedia.value = !showMedia.value
}

function handleKeydown(event: KeyboardEvent) {
  if (event.key === 'Escape' && showMedia.value) {
    showMedia.value = false
  }
}

onMounted(() => {
  window.addEventListener('keydown', handleKeydown)
})

onBeforeUnmount(() => {
  window.removeEventListener('keydown', handleKeydown)
})
</script>

<template>

  <lazy-attachment :attachment="props.attachment" @click="toggleModal" role="button" >
    <div class="modal d-flex attachment-modal" :class="modalClass" v-if="showMedia" @click.self="toggleModal">
      <div class="modal-dialog">
        <div class="modal-content attachment-content d-flex align-items-center">
          <button type="button" class="btn-close attachment-close" :aria-label="t('close')" @click="toggleModal"></button>
          <lazy-attachment :attachment="props.attachment"/>
          <div class="attachment-hint">{{ t('searchHintClose') }}</div>
        </div>
      </div>
    </div>
  </lazy-attachment>




</template>

<style scoped>
.attachment-modal {
  background:
    radial-gradient(circle at 20% 20%, rgba(255, 255, 255, 0.12), transparent 45%),
    rgba(2, 6, 23, 0.75);
  backdrop-filter: blur(2px);
  position: fixed;
  inset: 0;
  align-items: center;
  justify-content: center;
  z-index: 1050;
}

.modal-dialog {
  max-width: min(860px, 90vw);
  width: 100%;
}

.attachment-content {
  background: var(--color-bg-elevated);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: 1rem;
  box-shadow: var(--shadow-lg);
  max-height: 85vh;
  max-width: min(900px, 92vw);
  position: relative;
}

.attachment-content :deep(.attachment) {
  background: transparent;
  border: none;
  box-shadow: none;
  padding: 0;
}

.attachment-content :deep(.attachment-media) {
  max-height: 75vh;
  object-fit: contain;
}

.attachment-content :deep(video.attachment-media),
.attachment-content :deep(audio.attachment-media) {
  width: 100%;
}

.attachment-close {
  position: absolute;
  top: 0.75rem;
  right: 0.75rem;
  width: 2rem;
  height: 2rem;
  border-radius: var(--radius-pill);
  background: rgba(15, 23, 42, 0.5);
  opacity: 0.8;
}

.attachment-close:hover {
  opacity: 1;
}

.attachment-close:focus-visible {
  outline: 2px solid var(--focus-ring);
  outline-offset: 2px;
}

.attachment-hint {
  margin-top: 0.75rem;
  font-size: 0.8rem;
  color: var(--color-text-subtle);
  text-align: center;
  width: 100%;
}
</style>
