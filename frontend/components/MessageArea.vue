<template>
  <div id="message-area"
       class="message-area flex-column col-12 h-100 overflow-auto"
       :class="dynamicClass"
       ref="messagesAreaElement"
  >

    <message-area-nav-bar/>
    <div id="infinite-list" class="message-list d-flex flex-column">
      <button v-if="hasNextPages" type="button" class="btn btn-light load-more" @click="loadMoreMessages">{{ t('loadMoreMessages') }}
      </button>
      <template v-for="(message, index) in messages" :key="index">
        <message-item
          :message="message"
          :highlight-until-date="store.highlightUntilDate"
          :ref="index === 0 ? 'firstMessageRef' : null"
        />
      </template>
    </div>

  </div>
</template>

<script setup lang="ts">
import MessageItem from "~/components/MessageItem.vue";
import {useMainStore} from "~/store";
import { useUiText } from "~/composables/useUiText";

const store = useMainStore()
const { t } = useUiText()
const props = defineProps(['mobile'])
const messagesAreaElement = ref(null)

const moreMessagesPath = computed(() => { return store.moreMessagesPath });

const {data: response, refresh} = await useFetch(moreMessagesPath)

const content = computed(() => {
  return response?.value?.content ?? []
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

function scrollToFirstMessage() {
  if (messagesAreaElement.value && messages.value.length > 0) {
    nextTick(() => {
      const firstElement = messagesAreaElement.value.querySelector('.message-item');
      if (firstElement) {
        const elementTop = firstElement.offsetTop;
        const containerHeight = messagesAreaElement.value.clientHeight;
        const scrollPosition = elementTop - containerHeight / 3;

        messagesAreaElement.value.scrollTo({
          top: Math.max(0, scrollPosition),
          behavior: 'smooth'
        });
      }
    });
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

watch(
    () => messages.value.length,
    (sizeOfMessages) => {
      if (sizeOfMessages === 0) {
        refresh()
      }
    }
)

watch(content, async (newContent, oldContent) => {
  store.updateMessages([...newContent.reverse().map((it: any) => store.toChatMessage(it)), ...messages.value])
  await nextTick()

  if (store.highlightUntilDate) {
    scrollToFirstMessage()
  } else {
    scrollBottom()
  }
})

</script>

<style scoped>
.message-area {
  background: linear-gradient(180deg, rgba(2, 6, 23, 0.98), rgba(15, 23, 42, 0.95));
}

.message-list {
  padding: 1rem 1.25rem 1.5rem;
  gap: 0.75rem;
}

.load-more {
  align-self: center;
  border-radius: var(--radius-pill);
  padding: 0.35rem 1rem;
  background: rgba(255, 255, 255, 0.92);
  border: 1px solid var(--color-border-strong);
  box-shadow: var(--shadow-sm);
}
</style>
