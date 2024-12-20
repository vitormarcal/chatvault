<script setup lang="ts">
import {useMainStore} from "~/store";

const store = useMainStore();

const showMedia = ref(false);
const imageImportRef = ref<HTMLInputElement | null>(null);
const imageUrl = ref<string | ArrayBuffer | null>(null);
const fileValid = ref(false);

const profileImagePath = computed(() =>
    useRuntimeConfig().public.api.getProfileImage.replace(':chatId', store.chatActive.chatId.toString())
);

const modalClass = computed(() => ({
  'fade show d-block': showMedia.value
}));

function resetFileInput() {
  if (imageImportRef.value) {
    imageImportRef.value.value = ''; // Limpa o input de arquivo
    fileValid.value = false;
    imageUrl.value = null;
  }
}

function toggleModal() {
  showMedia.value = !showMedia.value;
  if (!showMedia.value) {
    resetFileInput();
  }
}

function previewFile(file: File) {
  const reader = new FileReader();
  reader.onload = () => {
    imageUrl.value = reader.result;
  };
  reader.readAsDataURL(file);
}

function onFilePicked(event: Event) {
  const file = (event.target as HTMLInputElement)?.files?.[0];
  if (file) {
    fileValid.value = true;
    previewFile(file);
  }
}

async function uploadFile() {
  if (!imageImportRef.value?.files?.[0]) return;

  const file = imageImportRef.value.files[0];
  const formData = new FormData();
  formData.append("profile-image", file);

  try {
    await $fetch(profileImagePath.value, {
      method: "POST",
      body: formData,
    });
    store.reloadImageProfile = true;
    toggleModal(); // Fecha o modal após o upload
  } catch (error) {
    console.error("Error uploading file:", error);
    // Adicione um feedback para o usuário
  } finally {
    resetFileInput();
  }
}
</script>

<template>
  <div>
    <a role="button" aria-label="Change your avatar" @click="toggleModal">
      <profile-image :id="store.chatActive.chatId" :cache-url="true" />
      <icon-pencil-square class="position-relative" style="right: 1rem; top: 1rem" />
    </a>

    <div class="modal d-flex" :class="modalClass" v-if="showMedia">
      <div class="modal-content d-flex align-items-center m-auto">
        <profile-image
            :id="store.chatActive.chatId"
            :cache-url="false"
            :url-provided="imageUrl"
            style="width: 35%; height: auto"
        />
        <input
            class="form-control form-control-sm mt-3"
            @change="onFilePicked"
            accept=".jpg"
            ref="imageImportRef"
            type="file"
            aria-label="Select a profile image"
        />
        <div class="modal-footer mt-3">
          <button type="button" class="btn btn-primary" @click="uploadFile" :disabled="!fileValid">
            Save changes
          </button>
          <button type="button" class="btn btn-secondary" data-dismiss="modal" @click="toggleModal">
            Close
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
