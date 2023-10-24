<template>
  <div>


    <div class="navbar">navbar</div>
    <main class="container-fluid">
      <div class="row h-100">
        <chat-list :chats="chats"
                   :active-chat="chat"
                   :mobile="isMobile"
                   @update:chat-active="updateChatActive"
        />
        <message-area
            :mobile="isMobile"
            @update:chat-exited="updateChatExited"
            :chat="chat"/>
      </div>
    </main>

  </div>
</template>

<script setup lang="ts">
import ChatList from "~/components/ChatList.vue";
import MessageArea from "~/components/MessageArea.vue";

const listChatsAPIUrl = useRuntimeConfig().public.api.listChats
const {data: chats} = await useFetch(listChatsAPIUrl)
const chat = ref({})
const isMobile = ref(true)

function checkWindowSize() {
  isMobile.value = window.innerWidth <= 575;
}

function updateChatActive(item: any) {
  chat.value = item
}

function updateChatExited() {
  chat.value = {}
}

onMounted(() => {
  nextTick(() => {
    window.addEventListener('resize', checkWindowSize);
  })
})


</script>


<style>

main {
  width: 100vw;
  height: 90vh;
}

.navbar {
  background: cadetblue;
}

</style>
