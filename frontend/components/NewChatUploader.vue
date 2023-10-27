<script setup lang="ts">
const emit = defineEmits(['update:chats'])
const chatImportRef = ref(null)
const importChatResult = ref({})
const fileValid = ref(false)
const chatName = ref(null)
const importChatPath = computed(() => {
  if (chatName.value != null) {
    return useRuntimeConfig().public.api.importChatByName.replace(":chatName", chatName.value);
  }
})

const chatNameValid = computed(() => chatName.value !== null && chatName.value.trim() !== '')

const disableUpload = computed(() => {
  return !chatNameValid.value || fileValid.value === false
})

async function onFilePicked() {
  if (chatImportRef?.value?.files && chatImportRef?.value?.files[0]) {
    fileValid.value = true
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
      emit('update:chats')
    }
  }
}

function cancel() {
  emit('update:chats')
}

</script>

<template>
  <div class="m-auto col-md-3">
    <div class="form-control">
      <div class="alert alert-success" v-if="importChatResult.data" role="alert">
        {{ importChatResult.data }}
      </div>
      <div class="alert alert-warning" v-if="importChatResult.error" role="alert">
        Failed to import, check server error logs
      </div>

      <div class="form-group">
        <label for="formGroupExampleInput">Give this chat a name</label>
        <input type="text" class="form-control" id="formGroupExampleInput" v-model="chatName" placeholder="Chat name">
      </div>

      <div class="form-group mb-3 pt-4 border-top border-2">
        <label for="formFileSm" class="form-label text-black">Import a new chat</label>
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
        <button type="button" @click="cancel"
                class="btn btn-outline-secondary ml-2">Cancel
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>

</style>