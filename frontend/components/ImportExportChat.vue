<script setup lang="ts" xmlns="http://www.w3.org/1999/html">
import {useMainStore} from "~/store";
import { useUiText } from "~/composables/useUiText";
const props = defineProps(['allowDownloadAll'])
const store = useMainStore()
const { t } = useUiText()
const clickModal = ref(false)
const chatImportRef = ref(null)
const errorMessage = ref(undefined)
const disableUpload = ref(true)
const modalClass = computed(() => {
  return {
    'fade show d-block': !!clickModal.value
  }
})

const importChatPath = computed(() => useRuntimeConfig().public.api.importChatById.replace(":chatId", store.chatActive?.chatId?.toString()))

const linkDownload = computed(() => {
  if (props.allowDownloadAll) {
    return useRuntimeConfig().public.api.exportAllChats
  } else {
    return useRuntimeConfig().public.api.exportChatById.replace(":chatId", store.chatActive?.chatId?.toString())
  }
})

const chatName = computed(() => {
  if (props.allowDownloadAll) {
    return "all-chats.zip"
  } else {
    return store.chatActive.chatName + '.zip'
  }
})

function toggleModal() {
  clickModal.value = !clickModal.value
  errorMessage.value = undefined
  if (chatImportRef.value) {
    chatImportRef.value.value = ''
  }
}

async function onFilePicked() {
  if (chatImportRef?.value?.files && chatImportRef?.value?.files[0]) {
    disableUpload.value = false
  }
}

async function uploadFile() {
  if (chatImportRef?.value?.files && chatImportRef?.value?.files[0]) {
    store.loading = true
    const form = new FormData()
    form.append("file", chatImportRef.value.files[0]);
    $fetch(importChatPath.value, {
      method: "POST",
      body: form
    }).then(() => {
      store.loading = false
      chatImportRef.value = null
      store.clearMessages()
    }).catch(e => {
      store.loading = false
      errorMessage.value = e.data.detail
    })
  }
}

watch(
    () => store.chatActive.chatId,
    (chatId) => {
      disableUpload.value = true
      if (chatImportRef.value) {
        chatImportRef.value.value = ''
      }
    }
)
</script>

<template>
  <div class="chat-option mt-3">

    <button type="button" @click="toggleModal" class="btn btn-outline-primary btn-sm import-trigger" data-bs-toggle="modal">
      {{ t('importExport') }}
    </button>

    <div class="modal import-modal" :class="modalClass" tabindex="-1" v-if="clickModal">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">{{ t('importExportTitle') }}</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" @click="toggleModal"
                    :aria-label="t('close')"></button>
          </div>
          <div class="modal-body">
            <div class="content-surface">
              <div class="alert alert-warning" v-if="errorMessage" role="alert">
                {{ t('failedToImport') }}<br/>
                {{ errorMessage }}
              </div>

              <div class="form-group d-flex justify-content-center mb-3">
                <a class="d-block text-center"
                   :href="linkDownload"
                   :download="chatName"
                >
                  {{ t('getEntireChat') }}
                </a>
              </div>

              <div class="form-group mb-3 pt-4 border-top border-2" v-if="!allowDownloadAll">
                <p class="helper">{{ t('actionsApplyCurrentChat') }}</p>
                <label for="formFileSm" class="form-label">{{ t('importMessagesToChat') }}</label>
                <input class="form-control form-control-sm"
                       @change="onFilePicked"
                       accept=".zip,.txt"
                       id="formFileSm"
                       ref="chatImportRef"
                       type="file">
              </div>
              <div class="btn-group" role="group" v-if="!allowDownloadAll">
                <button type="button" :disabled="disableUpload" @click="uploadFile"
                        class="btn btn-outline-secondary ml-2">{{ t('upload') }}
                </button>
              </div>
            </div>


          </div>
          <div class="modal-footer">
            <button type="button" @click="toggleModal" class="btn btn-secondary" data-bs-dismiss="modal">{{ t('close') }}</button>
          </div>
        </div>
      </div>
    </div>

  </div>
</template>

<style scoped>
.import-trigger {
  border-radius: var(--radius-pill);
}

.import-modal {
  background:
    radial-gradient(circle at 18% 18%, rgba(255, 255, 255, 0.14), transparent 45%),
    rgba(2, 6, 23, 0.65);
  backdrop-filter: blur(2px);
  position: fixed;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1050;
}

.modal-dialog {
  max-width: 520px;
  width: calc(100% - 2rem);
}

.modal-content {
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-border);
  background: var(--color-surface);
  box-shadow: var(--shadow-md);
  overflow: hidden;
}

.modal-header {
  border-bottom: 1px solid var(--color-border-soft);
  background: linear-gradient(180deg, rgba(248, 249, 251, 0.9), rgba(248, 249, 251, 0.6));
  padding: 1.1rem 1.2rem 0.85rem;
}

.btn-close:focus-visible {
  outline: 2px solid var(--focus-ring);
  outline-offset: 2px;
  border-radius: var(--radius-pill);
}

.modal-title {
  font-weight: 600;
  letter-spacing: -0.01em;
}

.modal-body {
  padding: 1rem 1.2rem 0.8rem;
}

.content-surface {
  border-radius: var(--radius-md);
  border: 1px solid var(--color-border-soft);
  background: #fff;
  padding: 1rem;
}

.helper {
  color: var(--color-text-muted-dark);
  margin-bottom: 0.75rem;
}

.modal-footer {
  border-top: 1px solid var(--color-border-soft);
  padding: 0.8rem 1.2rem 1.1rem;
}

.modal-footer .btn {
  border-radius: var(--radius-pill);
}
</style>
