<script setup lang="ts">
import {useMainStore} from "~/store";
import {computed, reactive, watch} from "vue";

const emit = defineEmits(["refresh:page"]);

const store = useMainStore();

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
  <div class="col-12 col-md-3 h-100 overflow-auto">
    <gallery v-if="chatConfig.showGallery">
      <a href="#" class="h2" @click="toggleGallery">
        <rotable-arrow-icon/>
      </a>
    </gallery>
    <template v-else>
      <a href="#" class="h2 m-2" @click="toggleChatConfig">
        <rotable-arrow-icon/>
      </a>
      <div class="d-flex justify-content-between">
        <profile-image-uploader/>
        <chat-deleter @refresh:page="() => emit('refresh:page')"/>
      </div>

      <div class="mt-3">
        <label class="form-label">Chat Name</label>
        <div class="input-group">
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

      <div class="form-group">
        <label for="active-author">Active Author</label>
        <select class="form-control" v-model="store.authorActive" id="active-author">
          <option v-for="option in store.authors" :key="option" :value="option">
            {{ option }}
          </option>
        </select>
      </div>

      <div class="form-group">
        <label for="page-size">Page size</label>
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
          Page size must be a value between 1 and 2000
        </div>
      </div>

      <div class="d-flex btn-group">
        <import-export-chat/>
        <button
            class="btn btn-outline-primary btn-sm mt-3"
            @click="toggleGallery"
        >
          Open the media gallery
          <rotable-arrow-icon degree="180"/>
        </button>
      </div>
    </template>
  </div>
</template>

<style scoped>
</style>
