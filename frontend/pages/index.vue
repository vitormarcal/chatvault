<template>
  <div>


    <div class="navbar">navbar</div>
    <main class="container-fluid">
      <div class="row h-100">
        <chat-list :chats="chats"
                   @update:chat-active="updateChatActive"
        />
        <message-area :chat="chat"/>
      </div>
    </main>

  </div>
</template>

<script setup lang="ts">
import ChatList from "~/components/ChatList.vue";
import MessageArea from "~/components/MessageArea.vue";

const listChatsAPIUrl = useRuntimeConfig().public.api.listChats
const { data: chats} = await useFetch(listChatsAPIUrl)
const chat = ref({})

function updateChatActive(item: any) {
  chat.value = item
}

</script>


<style>


.navbar {
  background: cadetblue;
}

#message-area {
  background: #0f6674;
}

</style>
