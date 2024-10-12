<template>
  <div class="index-page" ref="indexRef">

    <main class="container-fluid">
      <div class="d-flex justify-content-center" v-if="store.loading">
        <strong>Loading...</strong>
        <div class="spinner-border ml-auto" role="status" aria-hidden="true"></div>
      </div>
      <div class="row h-100 m-3 m-md-4">
        <new-chat-uploader v-if="createChatAction"
                           @update:chats="refreshPage"
                           @exit:dialog="() => createChatAction = false"
        >

        </new-chat-uploader>

        <chat-exporter v-else-if="exportChatAction"
                            allow-download-all="true"
                            @exit:dialog="() => exportChatAction = false" />

        <template v-else>
          <chat-list :chats="chats"
                     :mobile="isMobile"
                     @create:chat="createNewChat"
                     @update:chat-active="updateChatActive"
                     @export:chat="exportChat"
                     @update:disk-import="refreshPage"
          />
          <message-area
              :mobile="isMobile"
          />
          <chat-config v-if="store.chatConfigOpen"
                       @refresh:page="() => refresh()"
          />
        </template>

      </div>
    </main>

  </div>
</template>

<script setup lang="ts">
import ChatList from "~/components/ChatList.vue";
import MessageArea from "~/components/MessageArea.vue";
import ChatConfig from "~/components/ChatConfig.vue";
import {useMainStore} from "~/store";

const store = useMainStore()
const listChatsAPIUrl = useRuntimeConfig().public.api.listChats
const {data: chats, refresh} = await useFetch(listChatsAPIUrl)
const isMobile = ref(true)
const indexRef = ref(null)
const createChatAction = ref(false)
const exportChatAction = ref(false)

function checkWindowSize() {
  if (indexRef.value) {
    isMobile.value = indexRef.value.offsetWidth <= 575;
  }

}

function refreshPage() {
  store.loading = true
  createChatAction.value = false
  exportChatAction.value = false
  refresh()
  store.loading = false
}

function createNewChat() {
  createChatAction.value = true
}

function exportChat() {
  exportChatAction.value = true
}

function updateChatActive(item: any) {
  store.openChat(item)
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
  font-size: 18px;
  color: #ffff;

}

main {
  width: 100vw;
  height: 90vh;
}

</style>
