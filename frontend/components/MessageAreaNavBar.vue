<template>
  <div id="navbar"
       class="sticky-top d-flex p-2 m-0 justify-content-between"
       v-if="chatActive">
    <div :class="{ 'blur-sensitive': store.blurEnabled }" class="chat-info-header d-flex align-items-center">
      <a href="#" class="h2" @click="exitThisChat">
        <rotable-arrow-icon/>
      </a>
      <a href="#" class="m-2" @click="() => toggleOpenChatConfig()">
        <profile-image :id="store.chatActive.chatId"/>
      </a>
      <div class="d-flex flex-column" role="button" @click="() => toggleOpenChatConfig()">
        <div class="font-weight-bold" id="name">{{ store.chatActive.chatName }}
        </div>
        <span class="badge align-content-end bg-success">{{ store.chatActive.msgCount }} messages</span>
        <div class="small d-flex" id="details">last message sent:
          <message-created-at :date="store.chatActive.msgCreatedAt"/>
        </div>
      </div>

    </div>
    <search-bar :chatId="store.chatActive.chatId" @search="handleSearch" />
    <icon-three-dots class="self" role="button" @click="() => toggleOpenChatConfig()"/>

  </div>
</template>

<script setup lang="ts">
import {useMainStore} from '~/store';

const store = useMainStore();
const chatActive = computed(() => store.chatActive.chatId > 0);

function exitThisChat() {
  store.chatExited();
}

function toggleOpenChatConfig() {
  store.chatConfigOpen = !store.chatConfigOpen;
}

function handleSearch({ query, chatId }: { query: string; chatId: string | null }) {
  console.log("Search query:", query, "Chat ID:", chatId);
  // todo: post search
}

</script>

<style scoped>
#navbar {
  background: #000000;
}

.blur-sensitive {
  filter: blur(6px);
  transition: filter 0.3s ease-in-out;
}

#navbar:hover .blur-sensitive {
  filter: none;
}

.navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.5rem;
  background: #000;
  color: #fff;
}
</style>
