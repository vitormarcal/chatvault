<script setup lang="ts">
import {useMainStore} from "~/store";

const store = useMainStore()
const chatName = ref(store.chatActive.chatName)
const editChatName = ref(false)
const invalidPageSize = ref(false)
const showGallery = ref(false)

const invalidPageSizeClass = computed(() => ({'d-block': invalidPageSize.value}))

async function updateChatName() {
  const path = useRuntimeConfig().public.api.updateChatNameByChatId.replace(":chatId", store.chatActive.chatId.toString()).replace(":chatName", chatName.value)
  await $fetch(path, {method: 'PATCH'})
  store.chatActive.chatName = chatName.value
}

function toggleChatName() {
  editChatName.value = !editChatName.value
  if (!editChatName.value && chatName.value !== store.chatActive.chatName) {
    updateChatName()
  }
}

watch(
    () => store.chatActive.chatId,
    (chatId) => {
      chatName.value = store.chatActive.chatName
    }
)

function validatedPageSize(event: any) {
  event.preventDefault()
  let pageSizeNumber = event.target.value;
  const updated = store.updatePageSize(pageSizeNumber)
  invalidPageSize.value = !updated
}
</script>

<template>
  <div class="col-12 col-md-3 h-100 overflow-auto">
    <gallery v-if="showGallery">
      <a href="#" class="h2" @click="() => showGallery = !showGallery">
        <icon-arrow-left/>
      </a>
    </gallery>
    <template v-else>
      <profile-image :id="store.chatActive.chatId"/>

      <div class="mt-3">
        <label class="form-label">Chat Name</label><br/>
        <div class="input-group">

      <span class="input-group-text" @click="toggleChatName()">
        <icon-check v-if="editChatName"/>
        <icon-pencil-square v-else/>
      </span>
          <input type="text" :disabled="!editChatName" id="chatname-inpiut" class="form-control" v-model="chatName"
                 placeholder="Input group example" aria-label="Input group example" aria-describedby="basic-addon1"
                 data-ddg-inputtype="unknown">
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

      <import-export-chat
          :chat="store.chatActive"
      />
      <a href="#" class="h2" @click="() => showGallery = !showGallery">
        <icon-arrow-left/>
      </a>
    </template>


  </div>
</template>

<style scoped>

</style>