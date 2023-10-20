<template>
  <div id="message-area" class="message-area flex-column col-12 col-md-9 p-3 h-100 overflow-auto"
       ref="messagesAreaElement"
  >

    <div id="navbar" class="row d-flex flex-row align-items-center p-2 m-0" v-if="chatActive">
      <a href="#" class="h2">arrow</a>
      <a href="#">
        <profile-image :chat-id="chat.id"/>
      </a>
      <div class="d-flex flex-column">
        <div class="text-white font-weight-bold" id="name">{{ chat.chatName }}</div>
        <div class="text-white small" id="details">last seen 01/01/20233</div>
      </div>
    </div>


    <div id="infinite-list" class="message-list d-flex flex-column">
      <button v-if="hasNextPages" type="button" class="btn btn-light" @click="loadMoreMessages">Load more messages</button>
      <template v-for="(message, index) in messages" :key="index">
        <message-item :message="message" :chatId="chat.chatId"/>
      </template>
    </div>


  </div>
</template>

<script setup lang="ts">
import MessageItem from "~/components/MessageItem.vue";

const props = defineProps(['chat'])
const messages = ref([])
const nextPage = ref(0)
const messagesAreaElement = ref(null)

const chatActive = computed(() => props.chat.chatId > 0)

const moreMessagesPath = computed(() =>
    useRuntimeConfig().public.api.getMessagesByIdAndPage.replace(":chatId", props.chat.chatId).replace(":page", nextPage.value))


const {data: response} = await useLazyFetch(() => moreMessagesPath.value, {
  immediate: false
})


const content = computed(() => {
  return response?.value?.content ?? []
})

const hasNextPages = computed(() => {
  if (response?.value) {
    return !response.value.last
  } else {
    return false
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
   nextPage.value+=1
}

watch(
    () => props.chat.chatId,
    (chatId) => {
      messages.value = []
      nextPage.value = 0
    }
)

watch(content, async (newContent, oldContent) => {
  messages.value =  [...newContent, ...messages.value]
  await nextTick()
  scrollBottom()
})

</script>

<style scoped>

</style>