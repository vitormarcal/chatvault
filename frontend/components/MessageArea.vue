<template>
  <div id="message-area"
       class="message-area flex-column col-12 h-100 overflow-auto"
       :class="dynamicClass"
       ref="messagesAreaElement"
  >

    <div id="navbar"
         class="sticky-top d-flex p-2 m-0 justify-content-between"
         v-if="chatActive">
      <div class="chat-info-header d-flex align-items-center">
        <a href="#" class="h2" @click="exitThisChat">
          <icon-arrow-left/>
        </a>
        <a href="#" class="m-2" @click="() => toggleOpenChatConfig()">
          <profile-image :chat-id="chat.id"/>
        </a>
        <div class="d-flex flex-column" role="button" @click="() => toggleOpenChatConfig()">
          <div class="font-weight-bold" id="name">{{ chat.chatName }}</div>
          <div class="small d-flex" id="details">last message sent:
            <message-created-at :date="chat.msgCreatedAt"/>
          </div>
        </div>

      </div>

    </div>
    <div id="infinite-list" class="message-list d-flex flex-column">
      <button v-if="hasNextPages" type="button" class="btn btn-light" @click="loadMoreMessages">Load more messages
      </button>
      <template v-for="(message, index) in messages" :key="index">
        <message-item :message="message" :chatId="chat.chatId"/>
      </template>
    </div>

  </div>
</template>

<script setup lang="ts">
import MessageItem from "~/components/MessageItem.vue";
import {useMainStore} from "~/store";

const store = useMainStore()
const props = defineProps(['chat', 'mobile'])
const emit = defineEmits(['update:chat-exited'])
const messagesAreaElement = ref(null)

const chatActive = computed(() => props.chat.chatId > 0)

const moreMessagesPath = computed(() =>
    useRuntimeConfig().public.api.getMessagesByIdAndPage.replace(":chatId", props.chat.chatId).replace(":page", store.nextPage).replace(":size", store.pageSize))

const {data: response, refresh} = await useFetch(moreMessagesPath)

const content = computed(() => {
  return response?.value?.content ?? []
})

const authors = computed(() => {
  return store.authors
})

const messages = computed(() => store.messages)

const hasNextPages = computed(() => {
  if (response?.value) {
    return !response.value.last
  } else {
    return false
  }
})

const dynamicClass = computed(() => {
  return {
    'd-none': props.mobile && (props.chat.chatId == null || store.chatConfigOpen),
    'col-md-6': !props.mobile && store.chatConfigOpen,
    'col-md-9': !props.mobile && !store.chatConfigOpen,
  }
})

function scrollBottom() {
  if (messagesAreaElement.value && store.nextPage === 0) {
    messagesAreaElement.value.scrollTo({
      top: messagesAreaElement.value.scrollHeight,
      behavior: 'smooth'
    })
  }
}

function loadMoreMessages() {
  store.toNextPage()
}

function exitThisChat() {
  emit('update:chat-exited')
}

function toggleOpenChatConfig() {
  store.chatConfigOpen = !store.chatConfigOpen
}

watch(
    () => props.chat.chatId,
    (chatId) => {
      store.clearMessages()
    }
)

watch(content, async (newContent, oldContent) => {
  store.updateMessages([...newContent.reverse(), ...messages.value])
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