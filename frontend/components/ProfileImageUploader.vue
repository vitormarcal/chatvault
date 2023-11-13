<script setup lang="ts">
import {useMainStore} from "~/store";

const store = useMainStore()

let showMedia = ref(false);
const imageImportRef = ref(null)
const importChatResult = ref({})
const fileValid = ref(false)

const importChatPath = computed(() => {
  return useRuntimeConfig().public.api.importChatByName;

})

const modalClass = computed(() => {
  return {
    'fade show d-block': !!showMedia.value
  }
})

async function onFilePicked() {
  if (imageImportRef?.value?.files && imageImportRef?.value?.files[0]) {
    fileValid.value = true
  }
}

async function uploadFile() {
  if (imageImportRef?.value?.files && imageImportRef?.value?.files[0]) {
    let input = imageImportRef.value;
    const file = input.files[0]
    console.log(file)

    const form = new FormData()
    form.append("file", file);
    importChatResult.value = await useAsyncData(`upload ${file.name}`, () => $fetch(importChatPath.value, {
      method: "POST",
      body: form
    }))
    imageImportRef.value.value = ''
    if (importChatResult.value.data) {
      //do something
    }
  }
}

function toggleModal() {
  showMedia.value = !showMedia.value
}

</script>

<template>

  <div>

    <a role="button" aria-label="Change your avatar" @click="toggleModal">
      <profile-image :id="store.chatActive.chatId"/>
      <icon-pencil-square class="position-relative" style="right: 1rem;top: 1rem"/>

    </a>

    <div class="modal d-flex" :class="modalClass" v-if="showMedia">
      <div class="modal-content d-flex align-items-center m-auto">
        <profile-image :id="store.chatActive.chatId" style="width: 35%; height: auto"/>
        <input class="form-control form-control-sm"
               @change="onFilePicked"
               accept=".jpg"
               id="formFileSm"
               ref="imageImportRef"
               type="file">
        <div class="modal-footer">
          <button type="button" class="btn btn-primary" @click="uploadFile">Save changes</button>
          <button type="button" class="btn btn-secondary" data-dismiss="modal" @click="toggleModal">Close</button>
        </div>
      </div>
    </div>
  </div>

</template>

<style scoped>

</style>