<script setup lang="ts">
import {useMainStore} from "~/store";
import {computed, reactive, watch} from "vue";
import { LOCALE_DISPLAY_NAMES, SUPPORTED_LOCALES } from "~/types/localization";
import { useDateFormatting } from "~/composables/useDateFormatting";
import { useUiText } from "~/composables/useUiText";

const emit = defineEmits(["refresh:page"]);

const store = useMainStore();
const { systemLocale } = useDateFormatting();
const { t } = useUiText();

const chatConfig = reactive({
  chatName: store.chatActive.chatName,
  editChatName: false,
  invalidPageSize: false,
  showGallery: false,
});

const invalidPageSizeClass = computed(() => ({"d-block": chatConfig.invalidPageSize}));

watch(
    () => store.chatActive.chatId,
    () => {
      chatConfig.chatName = store.chatActive.chatName;
    }
);

const toggleChatConfig = () => (store.chatConfigOpen = !store.chatConfigOpen)

const toggleGallery = () => {
  chatConfig.showGallery = !chatConfig.showGallery;
};

const toggleChatName = async () => {
  chatConfig.editChatName = !chatConfig.editChatName;
  if (!chatConfig.editChatName && chatConfig.chatName !== store.chatActive.chatName) {
    await updateChatName();
  }
};

const updateChatName = async () => {
  const path = useRuntimeConfig()
      .public.api.updateChatNameByChatId.replace(":chatId", store.chatActive.chatId.toString())
      .replace(":chatName", chatConfig.chatName);
  await $fetch(path, {method: "PATCH"});
  store.chatActive.chatName = chatConfig.chatName;
};

const validatedPageSize = (event: Event) => {
  event.preventDefault();
  const input = event.target as HTMLInputElement;
  const pageSizeNumber = Number(input.value);
  const updated = store.updatePageSize(pageSizeNumber);
  chatConfig.invalidPageSize = !updated;
};
</script>

<template>
  <div class="col-12 col-md-3 h-100 overflow-auto config-panel">
    <gallery v-if="chatConfig.showGallery">
      <a href="#" class="h2" @click="toggleGallery">
        <rotable-arrow-icon/>
      </a>
    </gallery>
    <template v-else>
      <a href="#" class="h2 m-2 back-button" @click="toggleChatConfig">
        <rotable-arrow-icon/>
      </a>
      <div class="d-flex justify-content-between align-items-center header-actions">
        <profile-image-uploader/>
        <chat-deleter @refresh:page="() => emit('refresh:page')"/>
      </div>

      <div class="mt-3 config-section">
        <div class="section-title">{{ t('chatSettingsTitle') }}</div>
        <label class="form-label">{{ t('chatNameLabel') }}</label>
        <div class="input-group name-input">
          <span class="input-group-text" @click="toggleChatName">
            <icon-check v-if="chatConfig.editChatName"/>
            <icon-pencil-square v-else/>
          </span>
          <input
              type="text"
              :disabled="!chatConfig.editChatName"
              id="chatname-input"
              class="form-control"
              v-model="chatConfig.chatName"
          />
        </div>
      </div>

      <div class="form-group config-section">
        <label for="active-author">{{ t('activeAuthorLabel') }}</label>
        <select class="form-control" v-model="store.authorActive" id="active-author">
          <option v-for="option in store.authors" :key="option" :value="option">
            {{ option }}
          </option>
        </select>
      </div>

      <div class="form-group config-section">
        <label for="page-size">{{ t('pageSizeLabel') }}</label>
        <input
            class="form-control"
            type="number"
            max="2000"
            min="1"
            placeholder="20"
            id="page-size"
            @input="validatedPageSize"
        />
        <div class="invalid-feedback" :class="invalidPageSizeClass">
          {{ t('pageSizeInvalid') }}
        </div>
      </div>

      <div class="form-group config-section">
        <label for="locale-select">{{ t('dateFormatLocaleLabel') }}</label>
        <select class="form-control" v-model="store.userLocale" id="locale-select">
          <option v-for="locale in SUPPORTED_LOCALES" :key="locale" :value="locale">
            {{ LOCALE_DISPLAY_NAMES[locale] }}
            <span v-if="locale === 'auto'"> ({{ systemLocale }})</span>
          </option>
        </select>
        <small class="form-text text-muted d-block mt-2">
          {{ t('dateFormatLocaleHelp') }}
        </small>
      </div>

      <div class="d-flex btn-group config-actions">
        <import-export-chat/>
        <button
            class="btn btn-outline-primary btn-sm mt-3"
            @click="toggleGallery"
        >
          {{ t('openMediaGallery') }}
          <rotable-arrow-icon degree="180"/>
        </button>
      </div>
    </template>
  </div>
</template>

<style scoped>
.config-panel {
  background: linear-gradient(180deg, rgba(2, 6, 23, 0.98), rgba(15, 23, 42, 0.98));
  border-left: 1px solid var(--color-border);
  color: var(--color-text);
  padding: 0.75rem 0.9rem 1.25rem;
}

.back-button {
  color: var(--color-text);
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-pill);
  padding: 0.2rem 0.35rem;
  transition: background-color 0.2s ease, transform 0.2s ease;
}

.back-button:hover {
  background: rgba(148, 163, 184, 0.16);
  transform: translateY(-1px);
}

.header-actions {
  padding: 0.4rem 0.25rem 0.1rem;
}

.config-section {
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(148, 163, 184, 0.15);
  border-radius: 12px;
  padding: 0.85rem;
  margin-top: 0.75rem;
}

.section-title {
  font-size: 0.8rem;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: var(--color-text-muted);
  margin-bottom: 0.6rem;
}

.config-panel .form-label,
.config-panel label {
  color: var(--color-text);
  font-weight: 500;
  margin-bottom: 0.4rem;
}

.config-panel .form-control,
.config-panel .input-group-text {
  background: rgba(255, 255, 255, 0.08);
  border-color: var(--color-border-strong);
  color: var(--color-text);
  border-radius: 10px;
}

.config-panel .form-control::placeholder {
  color: var(--color-text-subtle);
}

.config-panel .form-control:focus {
  border-color: var(--color-accent-strong);
  box-shadow: 0 0 0 0.2rem rgba(59, 130, 246, 0.2);
}

.name-input .input-group-text {
  cursor: pointer;
  border-radius: 10px 0 0 10px;
}

.config-panel .form-text {
  color: var(--color-text-muted);
}

.config-panel .invalid-feedback {
  color: #fca5a5;
}

.config-actions {
  margin-top: 0.75rem;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.config-actions .btn {
  border-radius: var(--radius-pill);
}
</style>
