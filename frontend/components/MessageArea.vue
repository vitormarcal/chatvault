<template>
  <div class="message-area flex-column col-12 col-md-9 p-3 h-100" ref="chatWindow"
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


    <div class="message-list d-flex flex-column">
      <template v-for="(message, index) in messages" :key="index">
        <message-item :message="message" :chatId="chat.chatId"/>
      </template>
    </div>


  </div>
</template>

<script setup lang="ts">
import MessageItem from "~/components/MessageItem.vue";

const getMessagesByIdAndPageUrl = useRuntimeConfig().public.api.getMessagesByIdAndPage

const props = defineProps(['chat'])
const messages = ref([])
const nextPage = ref(0)

const chatActive = computed(() => props.chat.chatId > 0)

const moreMessagesPath = computed(() =>
    getMessagesByIdAndPageUrl.replace(":chatId", props.chat.chatId).replace(":page", nextPage.value))


const {data: response} = await useLazyFetch(() => moreMessagesPath.value, {
  immediate: false
})


const content = computed(() => {
  return response?.value?.content ?? []
})

watch(
    () => props.chat.chatId,
    (chatId) => {
      messages.value = []
    }
)

watch(content, async(newContent, oldContent) => {
  messages.value.push(...newContent)
})


</script>

<style scoped>

</style>