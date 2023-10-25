<template>
  <div id="message-area"
       class="message-area flex-column col-12 col-md-9 h-100 overflow-auto"
       :class="dynamicClass"
       ref="messagesAreaElement"
  >

    <div id="navbar"
         class="sticky-top d-flex p-2 m-0 justify-content-between"
         v-if="chatActive">
      <div class="chat-info-header d-flex align-items-center">
        <a href="#" class="h2" @click="exitThisChat">
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-arrow-left"
               viewBox="0 0 16 16">
            <path fill-rule="evenodd"
                  d="M15 8a.5.5 0 0 0-.5-.5H2.707l3.147-3.146a.5.5 0 1 0-.708-.708l-4 4a.5.5 0 0 0 0 .708l4 4a.5.5 0 0 0 .708-.708L2.707 8.5H14.5A.5.5 0 0 0 15 8z"/>
          </svg>
        </a>
        <a href="#" class="m-2">
          <profile-image :chat-id="chat.id"/>
        </a>
        <div class="d-flex flex-column">
          <div class="font-weight-bold" id="name">{{ chat.chatName }}</div>
          <div class="small d-flex" id="details">last message sent:
            <message-created-at :date="chat.msgCreatedAt"/>
          </div>
        </div>

      </div>

      <div class="chat-option d-flex mt-3">
        <div class="form-group">
          <label for="active-author">Active Author</label>
          <select class="form-control" v-model="authorActive" id="active-author">
            <option v-for="option in authors" :value="option">{{ option }}</option>
          </select>
        </div>

        <div class="form-group">
          <label for="import-chat-file">Import Chat</label>
          <input type="file" accept=".zip,.txt" ref="chatImportRef" class="form-control-file" id="import-chat-file"
                 @change="onFilePicked">
        </div>
      </div>


    </div>


    <div id="infinite-list" class="message-list d-flex flex-column">
      <button v-if="hasNextPages" type="button" class="btn btn-light" @click="loadMoreMessages">Load more messages
      </button>
      <template v-for="(message, index) in messages" :key="index">
        <message-item :message="message" :chatId="chat.chatId" :authorActive="authorActive"/>
      </template>
    </div>


  </div>
</template>

<script setup lang="ts">
import MessageItem from "~/components/MessageItem.vue";

const props = defineProps(['chat', 'mobile'])
const emit = defineEmits(['update:chat-exited'])

const messages = ref([])
const authorActive = ref({})
const nextPage = ref(0)
const messagesAreaElement = ref(null)
const chatImportRef = ref(null)

const chatActive = computed(() => props.chat.chatId > 0)

const moreMessagesPath = computed(() =>
    useRuntimeConfig().public.api.getMessagesByIdAndPage.replace(":chatId", props.chat.chatId).replace(":page", nextPage.value))

const importChatPath = computed(() => useRuntimeConfig().public.api.importChatById.replace(":chatId", props.chat.chatId))


const {data: response, refresh} = await useFetch(moreMessagesPath)


const content = computed(() => {
  return response?.value?.content ?? []
})

const authors = computed(() => {
  return [...new Set(messages.value.map(it => it.author))].filter(it => !!it)
})

const hasNextPages = computed(() => {
  if (response?.value) {
    return !response.value.last
  } else {
    return false
  }
})

const dynamicClass = computed(() => {
  return {
    'd-none': props.mobile && props.chat.chatId == null
  }
})

function scrollBottom() {
  if (messagesAreaElement.value && nextPage.value === 0) {
    messagesAreaElement.value.scrollTo({
      top: messagesAreaElement.value.scrollHeight,
      behavior: 'smooth'
    })
  }
}

function loadMoreMessages() {
  nextPage.value += 1
}

function exitThisChat() {
  emit('update:chat-exited')
}
async function onFilePicked() {
  if (chatImportRef?.value?.files && chatImportRef?.value?.files[0]) {
    let input = chatImportRef.value;
    const file = input.files[0]

    const form = new FormData()
    form.append("file", file);
    await $fetch(importChatPath.value, {
      method: "POST",
      body: form
    });

    chatImportRef.value.value = ''
    await refresh()
  }


}

watch(
    () => props.chat.chatId,
    (chatId) => {
      messages.value = []
      nextPage.value = 0
      if (chatImportRef.value) {
        chatImportRef.value.value = ''
      }
    }
)

watch(content, async (newContent, oldContent) => {
  messages.value = [...newContent.reverse(), ...messages.value]
  await nextTick()
  scrollBottom()
})

</script>

<style scoped>
.message-area {
  background: #360d3c;
}


#navbar {
  background: #360d3c;
}
</style>