<script setup lang="ts">
import {useMainStore} from "~/store";

const store = useMainStore()

const chatConfig = ref({
  chatName: store.chatActive.chatName,
  editChatName: false,
  invalidPageSize: false,
  showGallery: false
})

const invalidPageSizeClass = computed(() => ({'d-block': chatConfig.value.invalidPageSize}))

async function updateChatName() {
  const path = useRuntimeConfig().public.api.updateChatNameByChatId.replace(":chatId", store.chatActive.chatId.toString()).replace(":chatName", chatConfig.value.chatName)
  await $fetch(path, {method: 'PATCH'})
  store.chatActive.chatName = chatConfig.value.chatName
}

function toggleChatName() {
  chatConfig.value.editChatName = !chatConfig.value.editChatName
  if (!chatConfig.value.editChatName && chatConfig.value.chatName !== store.chatActive.chatName) {
    updateChatName()
  }
}

watch(
    () => store.chatActive.chatId,
    (chatId) => {
      chatConfig.value.chatName = store.chatActive.chatName
    }
)

function validatedPageSize(event: any) {
  event.preventDefault()
  let pageSizeNumber = event.target.value;
  const updated = store.updatePageSize(pageSizeNumber)
  chatConfig.value.invalidPageSize = !updated
}
</script>

<template>
  <div class="col-12 col-md-3 h-100 overflow-auto">
    <gallery v-if="chatConfig.showGallery">
      <a href="#" class="h2" @click="() => chatConfig.showGallery = !chatConfig.showGallery">
        <rotable-arrow-icon/>
      </a>
    </gallery>
    <template v-else>

      <a href="#" class="h2 m-2">
        <rotable-arrow-icon @click="() => store.chatConfigOpen = !store.chatConfigOpen"/>
      </a>
      <profile-image :id="store.chatActive.chatId"/>

      <div class="mt-3">
        <label class="form-label">Chat Name</label><br/>
        <div class="input-group">

      <span class="input-group-text" @click="toggleChatName()">
        <icon-check v-if="chatConfig.editChatName"/>
        <icon-pencil-square v-else/>
      </span>
          <input type="text" :disabled="!chatConfig.editChatName" id="chatname-inpiut" class="form-control"
                 v-model="chatConfig.chatName">
        </div>
      </div>

      <div class="form-group">
        <label for="active-author">Active Author</label>
        <select class="form-control" v-model="store.authorActive" id="active-author">
          <option v-for="option in store.authors" :value="option">{{ option }}</option>
        </select>
      </div>

      <div class="form-group">
        <label for="page-size">Page size</label>
        <input class="form-control" type="number" max="2000" min="1" placeholder="20" id="page-size"
               @input="validatedPageSize">
        <div class="invalid-feedback" :class="invalidPageSizeClass">page size must be a value between 1 and 2000</div>
      </div>

      <div class="d-flex btn-group">
        <import-export-chat
        />

        <button class="btn btn-outline-primary btn-sm mt-3 "
                @click="() => chatConfig.showGallery = !chatConfig.showGallery">
          Open the media gallery
          <rotable-arrow-icon degree="180"/>
        </button>
      </div>


    </template>


  </div>
</template>

<style scoped>

</style>