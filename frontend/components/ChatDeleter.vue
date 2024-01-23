<script setup lang="ts">

import {useMainStore} from "~/store";

const emit = defineEmits(['refresh:page'])


const store = useMainStore()

let showMedia = ref(false);

const modalClass = computed(() => {
  return {
    'fade show d-block': !!showMedia.value
  }
})

function toggleModal() {
  showMedia.value = !showMedia.value
}

async function deleteChat() {
  const deleteChatByIdPath = useRuntimeConfig().public.api.deleteChatById.replace(':chatId', store.chatActive.chatId.toString())
  await $fetch(deleteChatByIdPath, {method: 'DELETE'})
  emit('refresh:page')
  store.chatExited()

  //
}

</script>

<template>
  <div>
    <button class="btn btn-outline-danger" @click="toggleModal" type="button">Delete Chat</button>

    <div class="modal d-flex" :class="modalClass" v-if="showMedia">
      <div class="modal-content d-flex align-items-center m-auto w-auto p-2 text-center ">
        <p>Are you sure you want to delete this chat?</p>
        <p>This will remove all the assets and messages in chat permanently from ChatVault.</p>
        <p>You cannot undo this action!</p>
        <p>Chat name: {{store.chatActive.chatName}}, {{store.chatActive.msgCount}} messages</p>
        <div class="modal-footer">
          <button type="button" class="btn btn-primary" @click="toggleModal" >Cancel</button>
          <button type="button" class="btn btn-outline-danger" data-dismiss="modal" @click="deleteChat">Delete now!</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.modal-content {
  background: #4e1e55;
}
</style>