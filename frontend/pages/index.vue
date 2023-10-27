<template>
  <div class="index-page" ref="indexRef">

    <main class="container-fluid">
      <div class="row h-100 m-3 m-md-4">
        <new-chat-uploader v-if="firstChatUpload"
                           @update:chats="refreshPage"
        >

        </new-chat-uploader>
        <template v-else>
          <chat-list :chats="chats"
                     :active-chat="chat"
                     :mobile="isMobile"
                     @create:chat="createNewChat"
                     @update:chat-active="updateChatActive"
          />
          <message-area
              :mobile="isMobile"
              @update:chat-exited="updateChatExited"
              :chat="chat"/>
        </template>

      </div>
    </main>

  </div>
</template>

<script setup lang="ts">
import ChatList from "~/components/ChatList.vue";
import MessageArea from "~/components/MessageArea.vue";

const listChatsAPIUrl = useRuntimeConfig().public.api.listChats
const {data: chats, refresh} = await useFetch(listChatsAPIUrl)
const chat = ref({})
const isMobile = ref(true)
const indexRef = ref(null)
const createChatAction = ref(false)

const firstChatUpload = computed(() => {
  return chats?.value?.length === 0 || createChatAction.value
})

function sleep(ms: number) {
  return new Promise(resolve => setTimeout(resolve, ms))
}


function checkWindowSize() {
  if (indexRef.value) {
    isMobile.value = indexRef.value.offsetWidth <= 575;
  }

}

function refreshPage() {
  sleep(2000).then(() => {
        createChatAction.value = false
        refresh()
      }
  )
}

function createNewChat() {
  createChatAction.value = true
}

function updateChatActive(item: any) {
  chat.value = item
}

function updateChatExited() {
  chat.value = {}
}

watchEffect(() => {
  if (indexRef.value) {
    indexRef.value.addEventListener('resize', checkWindowSize);
    checkWindowSize()
  }
})

onBeforeMount(() => {
  window.addEventListener("resize", checkWindowSize);
})

onMounted(() => {
  nextTick(() => {
    if (indexRef.value) {
      console.log("add event listener")
      indexRef.value.addEventListener('resize', checkWindowSize);
      checkWindowSize()
    }
  })
})


</script>


<style>
.index-page {
  color: white;
}

main {
  width: 100vw;
  height: 90vh;
}

</style>
