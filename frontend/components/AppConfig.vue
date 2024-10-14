<script setup lang="ts">

import {useMainStore} from "~/store";
const store = useMainStore()

const chatConfig = ref({
  chatName: undefined,
  editChatName: false,
  invalidPageSize: false,
  showGallery: false
})

function toggleChatName() {
  chatConfig.value.editChatName = !chatConfig.value.editChatName
  if (!chatConfig.value.editChatName && chatConfig.value.chatName !== store.chatActive.chatName) {
    updateChatName()
  }
}

async function updateAppConfigs() {
  //const path = useRuntimeConfig().public.api.updateChatNameByChatId.replace(":chatId", store.chatActive.chatId.toString()).replace(":chatName", chatConfig.value.chatName)
  //await $fetch(path, {method: 'PATCH'})
  //store.chatActive.chatName = chatConfig.value.chatName
}

</script>

<template>
  <div class="flex-column col-12 h-100 overflow-auto col-md-9">
    <div class="mt-3">
      <label class="form-label">Set an optional active username to be used in chats</label><br/>
      <div class="input-group">

      <span class="input-group-text" @click="toggleChatName()">
        <icon-check v-if="chatConfig.editChatName"/>
        <icon-pencil-square v-else/>
      </span>
        <input type="text" :disabled="!chatConfig.editChatName" id="chatname-inpiut" class="form-control"
               v-model="chatConfig.chatName">
      </div>
    </div>
  </div>
</template>

<style scoped>

</style>