<script setup lang="ts">
import {useMainStore} from "~/store";

const store = useMainStore()

const chatActive = computed(() => store.chatActive.chatId > 0)

function exitThisChat() {
  store.chatExited()
}

function toggleOpenChatConfig() {
  store.chatConfigOpen = !store.chatConfigOpen
}

</script>

<template>
  <div id="navbar"
       class="sticky-top d-flex p-2 m-0 justify-content-between"
       v-if="chatActive">
    <div class="chat-info-header d-flex align-items-center">
      <a href="#" class="h2" @click="exitThisChat">
        <rotable-arrow-icon/>
      </a>
      <a href="#" class="m-2" @click="() => toggleOpenChatConfig()">
        <profile-image :id="store.chatActive.chatId"/>
      </a>
      <div class="d-flex flex-column" role="button" @click="() => toggleOpenChatConfig()">
        <div class="font-weight-bold" id="name">{{ store.chatActive.chatName }}</div>
        <div class="small d-flex" id="details">last message sent:
          <message-created-at :date="store.chatActive.msgCreatedAt"/>
        </div>
      </div>

    </div>

  </div>
</template>

<style scoped>
#navbar {
  background: #360d3c;
}
</style>