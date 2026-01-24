<template>
  <div id="message-area"
       class="message-area flex-column col-12 h-100 overflow-auto"
       :class="dynamicClass"
       ref="messagesAreaElement"
  >

    <message-area-nav-bar/>
    <div id="infinite-list" class="message-list d-flex flex-column" :class="{ 'has-jump-footer': isJumpMode }">
      <button
        v-if="!isJumpMode && hasOlderMessages"
        type="button"
        class="btn btn-light load-more load-more-top load-more-contrast"
        @click="loadOlderMessages"
      >
        {{ t('loadMoreMessages') }}
      </button>
      <div v-if="isJumpMode" class="jump-header">
        <div class="jump-indicator">
          <span class="badge-pill">{{ t('jumpModeActive') }}</span>
          <span v-if="jumpModeDate" class="jump-date">{{ jumpModeDate }}</span>
        </div>
        <button
          v-if="hasOlderMessages"
          type="button"
          class="btn btn-light load-more load-more-contrast"
          @click="loadOlderMessages"
        >
          {{ t('loadOlderMessages') }}
        </button>
      </div>
      <template v-for="(message, index) in messages" :key="index">
        <message-item
          :message="message"
          :highlight-until-date="store.highlightUntilDate"
          :highlight-message-id="store.highlightMessageId"
          :enable-jump="isSearchMode"
          :ref="index === 0 ? 'firstMessageRef' : null"
          @jump="handleMessageJump"
        />
      </template>
      <div class="message-controls">
        <template v-if="isJumpMode">
          <button
            v-if="hasNewerMessages"
            type="button"
            class="btn btn-light load-more load-more-contrast"
            @click="loadNewerMessages"
          >
            {{ t('loadNewerMessages') }}
          </button>
        </template>
      </div>
      <div class="message-controls" :class="{ 'jump-footer': isJumpMode }">
        <template v-if="isJumpMode">
          <button
            type="button"
            class="btn btn-outline-light load-more back-to-latest"
            @click="returnToLatest"
          >
            {{ t('backToLatest') }}
          </button>
        </template>
      </div>
    </div>

  </div>
</template>

<script setup lang="ts">
import MessageItem from "~/components/MessageItem.vue";
import {useMainStore} from "~/store";
import { useUiText } from "~/composables/useUiText";
import { useDateFormatting } from "~/composables/useDateFormatting";

const store = useMainStore()
const { t } = useUiText()
const { formatDateFull } = useDateFormatting()
const props = defineProps(['mobile'])
const messagesAreaElement = ref(null)

const moreMessagesPath = computed(() => { return store.moreMessagesPath });

const {data: response, refresh} = await useFetch(moreMessagesPath)

const content = computed(() => {
  return response?.value?.content ?? []
})

const messageViewMode = computed(() => store.messageViewMode)
const messages = computed(() => {
  if (messageViewMode.value === 'context') return store.contextMessages
  if (messageViewMode.value === 'search') return store.searchResults
  return store.messages
})
const isSearchMode = computed(() => messageViewMode.value === 'search')
const isJumpMode = computed(() => store.paginationMode === 'jump')
const jumpModeDate = computed(() => {
  if (!store.highlightUntilDate) return ''
  const date = new Date(store.highlightUntilDate)
  if (Number.isNaN(date.getTime())) return store.highlightUntilDate
  return formatDateFull(date)
})

const hasOlderMessages = computed(() => {
  if (isSearchMode.value) {
    return false
  }

  if (isJumpMode.value) {
    return store.jumpHasMoreOlder
  }

  if (response?.value) {
    return !response.value.last
  } else {
    return false
  }
})

const hasNewerMessages = computed(() => {
  if (!isJumpMode.value) {
    return false
  }

  return store.jumpHasMoreNewer
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

function loadOlderMessages() {
  store.loadOlderMessages()
}

function loadNewerMessages() {
  if (!messagesAreaElement.value) {
    store.loadNewerMessages()
    return
  }

  const previousScrollTop = messagesAreaElement.value.scrollTop
  const previousScrollHeight = messagesAreaElement.value.scrollHeight

  store.loadNewerMessages().then(async () => {
    await nextTick()
    const newScrollHeight = messagesAreaElement.value?.scrollHeight ?? previousScrollHeight
    const delta = newScrollHeight - previousScrollHeight
    messagesAreaElement.value?.scrollTo({
      top: previousScrollTop + delta,
      behavior: 'auto',
    })
  })
}

function returnToLatest() {
  store.resetPaginationState()
  store.clearMessages()
  refresh()
}

function handleMessageJump(message: { id: number; createdAt: string }) {
  if (!isSearchMode.value) return
  if (!store.chatActive.chatId) return
  store.jumpToDate(store.chatActive.chatId, message.createdAt, message.id)
}

watch(
    () => store.chatActive.chatId,
    (chatId) => {
      store.resetPaginationState()
      store.clearMessages()
    }
)

watch(
    () => messages.value.length,
    (sizeOfMessages) => {
      if (messageViewMode.value === 'context') return
      if (messageViewMode.value === 'search') return
      if (isJumpMode.value) return
      if (sizeOfMessages === 0) {
        refresh()
      }
    }
)

watch(content, async (newContent, oldContent) => {
  if (messageViewMode.value === 'context') return
  if (messageViewMode.value === 'search') return
  if (isJumpMode.value) return
  store.updateMessages([...newContent.reverse().map((it: any) => store.toChatMessage(it)), ...messages.value])
  await nextTick()

  if (store.highlightUntilDate) {
    scrollToFirstMessage()
  } else {
    scrollBottom()
  }
})

watch(
  () => store.highlightUntilDate,
  async (highlightUntilDate) => {
    if (!highlightUntilDate || messages.value.length === 0) return
    await nextTick()
    scrollToFirstMessage()
  }
)

</script>

<style scoped>
.message-area {
  background: linear-gradient(180deg, rgba(2, 6, 23, 0.98), rgba(15, 23, 42, 0.95));
}

.message-list {
  padding: 1rem 1.25rem 1.5rem;
  gap: 0.75rem;
}

.message-list.has-jump-footer {
  padding-bottom: 6rem;
}

.message-controls {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.6rem;
  margin-top: 0.75rem;
}

.load-more-top {
  align-self: center;
  margin-bottom: 0.5rem;
}

.jump-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.45rem;
  position: sticky;
  top: 0.5rem;
  z-index: 2;
  padding: 0.25rem 0;
  width: fit-content;
  align-self: center;
}

.jump-indicator {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: rgba(226, 232, 240, 0.9);
  font-size: 0.85rem;
}

.jump-indicator .badge-pill {
  background: rgba(125, 211, 252, 0.18);
  border: 1px solid rgba(125, 211, 252, 0.5);
  color: #bae6fd;
  padding: 0.15rem 0.6rem;
  border-radius: 999px;
  font-weight: 600;
  letter-spacing: 0.01em;
}

.jump-indicator .jump-date {
  opacity: 0.75;
}

.jump-footer {
  position: sticky;
  bottom: 0.5rem;
  z-index: 2;
  padding: 0.35rem 0;
  width: fit-content;
  align-self: center;
}

.back-to-latest {
  background: rgba(248, 250, 252, 0.96);
  border-color: rgba(148, 163, 184, 0.6);
  color: #0f172a;
  font-weight: 600;
}

.back-to-latest:hover {
  border-color: rgba(148, 163, 184, 0.85);
  background: rgba(255, 255, 255, 1);
  color: #0f172a;
}

.load-more-contrast {
  background: rgba(248, 250, 252, 0.96);
  border-color: rgba(148, 163, 184, 0.6);
  color: #0f172a;
  font-weight: 600;
}

.load-more-contrast:hover {
  background: rgba(255, 255, 255, 1);
  border-color: rgba(148, 163, 184, 0.85);
  color: #0f172a;
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
