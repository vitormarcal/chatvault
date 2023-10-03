<template>
  <div>


    <div class="navbar">navbar</div>
    <main class="container-fluid">
      <div class="row h-100">
        <ChatList :chats="chats"
        />
        <div id="message-area"></div>
      </div>
    </main>

  </div>
</template>

<script>
import ChatList from "../components/ChatList.vue";

export default {
  name: 'IndexPage',
  components: {ChatList},
  data() {
    return {
      chats: []
    }
  },
  async mounted() {
    this.chats = await this.$getChatList()
  },
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
