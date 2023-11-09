<template>
  <div id="message-area"
       class="message-area flex-column col-12 h-100 overflow-auto"
       :class="dynamicClass"
       ref="messagesAreaElement"
  >

    <message-area-nav-bar/>
    <div id="infinite-list" class="message-list d-flex flex-column">
      <button v-if="hasNextPages" type="button" class="btn btn-light" @click="loadMoreMessages">Load more messages
      </button>
      <template v-for="(message, index) in messages" :key="index">
        <message-item :message="message"/>
      </template>
    </div>

  </div>
</template>

<script setup lang="ts">
import MessageItem from "~/components/MessageItem.vue";
import {useMainStore} from "~/store";

const store = useMainStore()
const props = defineProps(['mobile'])
const messagesAreaElement = ref(null)

const moreMessagesPath = computed(() =>
    useRuntimeConfig().public.api.getMessagesByIdAndPage.replace(":chatId", store.chatActive.chatId?.toString()).replace(":page", store.nextPage.toString()).replace(":size", store.pageSize.toString()))

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
    'd-none': props.mobile && (store.chatActive.chatId == null || store.chatConfigOpen),
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

watch(
    () => store.chatActive.chatId,
    (chatId) => {
      store.clearMessages()
    }
)

watch(content, async (newContent, oldContent) => {
  store.updateMessages([...newContent.reverse().map((it: any) => store.toChatMessage(it)), ...messages.value])
  await nextTick()
  scrollBottom()
})

</script>

<style scoped>
.message-area {
  background: #360d3c;
}
</style>