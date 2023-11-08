<script setup lang="ts">
import {useMainStore} from "~/store";

const store = useMainStore()
const props = defineProps(['chat'])
const clickModal = ref(false)
const chatImportRef = ref(null)
const importChatResult = ref({})
const disableUpload = ref(true)
const modalClass = computed(() => {
  return {
    'fade show d-block': !!clickModal.value
  }
})

const importChatPath = computed(() => useRuntimeConfig().public.api.importChatById.replace(":chatId", props.chat.chatId))
const downloadChatPath = computed(() => useRuntimeConfig().public.api.exportChatById.replace(":chatId", props.chat.chatId))


function toggleModal() {
  clickModal.value = !clickModal.value
  importChatResult.value = {}
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
    let input = chatImportRef.value;
    const file = input.files[0]
    console.log(file)

    const form = new FormData()
    form.append("file", file);
    importChatResult.value = await useAsyncData(`upload ${file.name}`, () => $fetch(importChatPath.value, {
      method: "POST",
      body: form
    }))
    chatImportRef.value.value = ''
    if (importChatResult.value.data) {
      store.clearMessages()
    }
  }
}


watch(
    () => props.chat.chatId,
    (chatId) => {
      disableUpload.value = true
      if (chatImportRef.value) {
        chatImportRef.value.value = ''
      }
    }
)
</script>

<template>
  <div class="chat-option mt-3 ">


    <button type="button" @click="toggleModal" class="btn btn-outline-primary btn-sm" data-bs-toggle="modal">
      Import/Export
    </button>


    <div class="modal" :class="modalClass" tabindex="-1">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title text-black">File importer/exporter</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" @click="toggleModal"
                    aria-label="Close"></button>
          </div>
          <div class="modal-body">
            <div class="form-control">
              <div class="alert alert-success" v-if="importChatResult.data" role="alert">
                {{ importChatResult.data }}
              </div>
              <div class="alert alert-warning" v-if="importChatResult.error" role="alert">
                Failed to import, check server error logs
              </div>

              <div class="form-group d-flex justify-content-center mb-3">
                <a class="d-block text-center"
                   :href="downloadChatPath"
                   :download="chat.chatName + '.zip'"
                >
                  Get the entire chat
                </a>
              </div>

              <div class="form-group mb-3 pt-4 border-top border-2">
                <p class="text-black">Attention, these actions are related to the selected chat! </p>
                <label for="formFileSm" class="form-label text-black">Import messages to this chat</label>
                <input class="form-control form-control-sm"
                       @change="onFilePicked"
                       accept=".zip,.txt"
                       id="formFileSm"
                       ref="chatImportRef"
                       type="file">
              </div>
              <div class="btn-group" role="group">
                <button type="button" :disabled="disableUpload" @click="uploadFile"
                        class="btn btn-outline-secondary ml-2">Upload
                </button>
              </div>
            </div>


          </div>
          <div class="modal-footer">
            <button type="button" @click="toggleModal" class="btn btn-secondary" data-bs-dismiss="modal">Close</button>
          </div>
        </div>
      </div>
    </div>

  </div>
</template>

<style scoped>

</style>