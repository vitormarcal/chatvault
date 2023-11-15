<script setup lang="ts">
import {useMainStore} from "~/store";

const store = useMainStore()

let showMedia = ref(false);
const imageImportRef = ref(null)
const importChatResult = ref({})
const fileValid = ref(false)
const imageUrl = ref(null as string | ArrayBuffer | null)

const profileImagePath = computed(() => {
  return useRuntimeConfig().public.api.getProfileImage.replace(':chatId', store.chatActive.chatId.toString());
})

const modalClass = computed(() => {
  return {
    'fade show d-block': !!showMedia.value
  }
})

async function onFilePicked() {
  if (imageImportRef?.value?.files && imageImportRef?.value?.files[0]) {
    store.reloadImageProfile = false
    fileValid.value = true
    const file = imageImportRef?.value?.files[0]
    const reader = new FileReader();
    reader.onload = () => {
      imageUrl.value = reader.result;
    };
    reader.readAsDataURL(file);
  }
}

async function uploadFile() {
  if (imageImportRef?.value?.files && imageImportRef?.value?.files[0]) {
    let input = imageImportRef.value;
    const file = input.files[0]
    console.log(file)

    const form = new FormData()
    form.append("profile-image", file);
    importChatResult.value = await useAsyncData(`upload ${file.name}`, () => $fetch(profileImagePath.value, {
      method: "POST",
      body: form
    }))
    imageImportRef.value.value = ''
    store.reloadImageProfile = true
  }
}

function toggleModal() {
  showMedia.value = !showMedia.value
  if (imageImportRef?.value?.files) {
    imageImportRef.value.files = []
    store.reloadImageProfile = false
    fileValid.value = false
    imageImportRef.value = null
  }
}

</script>

<template>

  <div>

    <a role="button" aria-label="Change your avatar" @click="toggleModal">
      <profile-image :id="store.chatActive.chatId" :cache-url="true"/>
      <icon-pencil-square class="position-relative" style="right: 1rem;top: 1rem"/>

    </a>

    <div class="modal d-flex" :class="modalClass" v-if="showMedia">
      <div class="modal-content d-flex align-items-center m-auto">
        <profile-image :id="store.chatActive.chatId" :cache-url="false" :url-provided="imageUrl"
                       style="width: 35%; height: auto"/>
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