<template>
  <div id="chat-list-area"
       class="col-12  col-md-3 flex-column overflow-auto h-100 p-0"
       :class="dynamicClass">
    <div class="action-bar d-flex flex-row p-2 sticky-top">
      <div class="btn-group" role="group">
        <button type="button" class="btn btn-outline-primary btn-sm" @click="emitCreateNewChat">Create new chat</button>
        <button type="button" class="btn btn-outline-primary btn-sm" @click="emitDiskImport">Execute Disk Import</button>
        <import-export-chat style="margin-top: 0 !important;"  allow-download-all="true" @click="() => exitThisChat()"/>
      </div>

    </div>
    <template v-for="item in chats">
      <chat-item :item="item"
                 @update:chat-active="emitThisChatActive"
      />
    </template>

  </div>
</template>
<script setup lang="ts">

import {useMainStore} from "~/store";

const store = useMainStore()

const props = defineProps(['chats', 'mobile'])
const emit = defineEmits(['update:chat-active', 'create:chat', 'update:disk-import'])

const chatOpened = computed(() => store.chatActive?.chatId != null)
const dynamicClass = computed(() => {
  return {
    'd-none': props.mobile && chatOpened.value,
    'd-flex': props.mobile && !chatOpened.value
  }
})

function emitThisChatActive(item: any) {
  emit('update:chat-active', item)
}

function emitCreateNewChat() {
  emit('create:chat')
}

async function emitDiskImport() {
  store.loading = true
  await useFetch(useRuntimeConfig().public.api.importFromDisk, {method: 'post'})
  store.loading = false
  emit('update:disk-import')
}

function exitThisChat() {
  store.chatExited()
}

</script>


<style>
.action-bar {
  background: #000000;
}
</style>
