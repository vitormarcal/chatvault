<script setup lang="ts">
import {useMainStore} from "~/store";
import { useUiText } from "~/composables/useUiText";

const store = useMainStore();
const { t } = useUiText();

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
    <a role="button" :aria-label="t('changeAvatarAria')" class="avatar-trigger" @click="toggleModal">
      <profile-image :id="store.chatActive.chatId" :cache-url="true" />
      <icon-pencil-square class="position-relative" style="right: 1rem; top: 1rem" />
    </a>

    <div class="modal d-flex avatar-modal" :class="modalClass" v-if="showMedia">
      <div class="modal-dialog">
        <div class="modal-content avatar-content">
          <div class="modal-header">
            <h5 class="modal-title">{{ t('updateAvatarTitle') }}</h5>
            <button type="button" class="btn-close" :aria-label="t('close')" @click="toggleModal"></button>
          </div>
          <div class="modal-body">
            <div class="preview-wrap">
              <profile-image
                  :id="store.chatActive.chatId"
                  :cache-url="false"
                  :url-provided="imageUrl"
              />
            </div>
            <input
                class="form-control form-control-sm mt-3"
                @change="onFilePicked"
                accept=".jpg"
                ref="imageImportRef"
                type="file"
                :aria-label="t('selectProfileImageAria')"
            />
            <small class="text-muted helper">{{ t('jpgOnlyHint') }}</small>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-outline-secondary" data-dismiss="modal" @click="toggleModal">
              {{ t('cancel') }}
            </button>
            <button type="button" class="btn btn-primary" @click="uploadFile" :disabled="!fileValid">
              {{ t('saveChanges') }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.avatar-trigger {
  position: relative;
  display: inline-flex;
  border-radius: var(--radius-pill);
  padding: 0.2rem;
  transition: background-color 0.2s ease, transform 0.2s ease;
}

.avatar-trigger:hover {
  background: rgba(148, 163, 184, 0.16);
  transform: translateY(-1px);
}

.avatar-modal {
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
  max-width: 460px;
  width: calc(100% - 2rem);
}

.avatar-content {
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-border);
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
  padding: 1rem 1.2rem 0.6rem;
  display: flex;
  flex-direction: column;
  gap: 0.6rem;
}

.preview-wrap {
  width: 120px;
  height: 120px;
  border-radius: 999px;
  overflow: hidden;
  border: 2px solid var(--color-border-strong);
  align-self: center;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
}

.preview-wrap :deep(img) {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.helper {
  color: var(--color-text-muted-dark);
}

.modal-footer {
  border-top: 1px solid var(--color-border-soft);
  padding: 0.8rem 1.2rem 1.1rem;
  display: flex;
  justify-content: flex-end;
  gap: 0.5rem;
}

.modal-footer .btn {
  border-radius: var(--radius-pill);
}
</style>
