<script setup lang="ts">

import {useMainStore} from "~/store";
import { useUiText } from "~/composables/useUiText";

const emit = defineEmits(['refresh:page'])


const store = useMainStore()
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

async function deleteChat() {
  const deleteChatByIdPath = useRuntimeConfig().public.api.deleteChatById.replace(':chatId', store.chatActive.chatId.toString())
  await $fetch(deleteChatByIdPath, {method: 'DELETE'})
  emit('refresh:page')
  store.chatExited()

  //
}

</script>

<template>
  <div>
    <button class="btn btn-outline-danger delete-trigger" @click="toggleModal" type="button">{{ t('deleteChat') }}</button>

    <div class="modal d-flex delete-modal" :class="modalClass" v-if="showMedia">
      <div class="modal-dialog">
        <div class="modal-content delete-content">
          <div class="modal-header">
            <h5 class="modal-title">{{ t('deleteChatTitle') }}</h5>
            <button type="button" class="btn-close" :aria-label="t('close')" @click="toggleModal"></button>
          </div>
          <div class="modal-body">
            <p class="lead">{{ t('deleteChatConfirmLead') }}</p>
            <p>{{ t('deleteChatConfirmBody') }}</p>
            <p class="warning">{{ t('deleteChatConfirmWarning') }}</p>
            <div class="chat-summary">
              <span class="chat-name">{{ store.chatActive.chatName }}</span>
              <span class="chat-meta">{{ t('chatSummaryMessages', { count: store.chatActive.msgCount }) }}</span>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-outline-light" @click="toggleModal">{{ t('cancel') }}</button>
            <button type="button" class="btn btn-danger" data-dismiss="modal" @click="deleteChat">{{ t('deleteNow') }}</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.delete-trigger {
  border-radius: var(--radius-pill);
}

.delete-modal {
  background:
    radial-gradient(circle at 20% 20%, rgba(255, 255, 255, 0.12), transparent 45%),
    rgba(2, 6, 23, 0.7);
  backdrop-filter: blur(2px);
  position: fixed;
  inset: 0;
  align-items: center;
  justify-content: center;
  z-index: 1050;
}

.modal-dialog {
  max-width: 420px;
  width: calc(100% - 2rem);
}

.delete-content {
  background: var(--color-bg-elevated);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  color: var(--color-text);
  box-shadow: var(--shadow-lg);
  overflow: hidden;
}

.modal-header {
  border-bottom: 1px solid rgba(148, 163, 184, 0.15);
  padding: 1.1rem 1.2rem 0.85rem;
}

.btn-close:focus-visible {
  outline: 2px solid #0d6efd;
  outline-offset: 2px;
  border-radius: 999px;
}

.modal-title {
  font-weight: 600;
  letter-spacing: -0.01em;
}

.modal-body {
  padding: 1rem 1.2rem 0.6rem;
}

.modal-body p {
  margin-bottom: 0.6rem;
  color: rgba(226, 232, 240, 0.85);
}

.modal-body .lead {
  color: #f8fafc;
  font-weight: 600;
}

.warning {
  color: #fca5a5;
  font-weight: 500;
}

.chat-summary {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid var(--color-border);
  padding: 0.6rem 0.75rem;
  margin-top: 0.7rem;
}

.chat-name {
  font-weight: 600;
}

.chat-meta {
  font-size: 0.8rem;
  color: var(--color-text-muted);
}

.modal-footer {
  border-top: 1px solid rgba(148, 163, 184, 0.15);
  padding: 0.8rem 1.2rem 1.1rem;
  display: flex;
  justify-content: flex-end;
  gap: 0.5rem;
}

.modal-footer .btn {
  border-radius: var(--radius-pill);
}
</style>
