<template>
  <div>


    <div class="navbar">navbar</div>
    <main class="container-fluid">
      <div class="row h-100">
        <chat-list :chats="chats"
        />
        <div id="message-area"></div>
      </div>
    </main>

  </div>
</template>

<script setup lang="ts">
import ChatList from "~/components/ChatList.vue";

const listChatsAPIUrl = useRuntimeConfig().public.api.listChats
const { data: chats} = await useFetch(listChatsAPIUrl)

</script>


<style>


.navbar {
  background: cadetblue;
}

#message-area {
  background: #0f6674;
}

</style>
