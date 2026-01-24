<template>
  <div id="chat-list-area"
       class="col-12 col-md-3 flex-column overflow-auto h-100 p-0"
       :class="dynamicClass">
    <div class="action-bar d-flex flex-row p-2 sticky-top">
      <div class="btn-group" role="group">
        <button type="button" class="btn btn-outline-primary btn-sm action-btn" @click="emitCreateNewChat">{{ t('createNewChat') }}</button>
        <button type="button" class="btn btn-outline-primary btn-sm action-btn" @click="emitDiskImport">{{ t('executeDiskImport') }}
        </button>
        <button type="button" class="btn btn-outline-primary btn-sm action-btn" @click="emitChatExport">{{ t('executeChatExport') }}
        </button>
        <button type="button" class="btn btn-outline-primary btn-sm action-btn" @click="store.toggleBlur">
          {{ store.blurEnabled ? t('disableBlur') : t('enableBlur') }}
        </button>
      </div>

    </div>
    <template v-for="item in chats">
      <chat-item :item="item"
                 @update:chat-active="emitThisChatActive"
      />
    </template>

  </div>
</template>
<script setup lang="ts">

import {useMainStore} from "~/store";
import { useUiText } from "~/composables/useUiText";

const store = useMainStore()
const { t } = useUiText()

const props = defineProps(['chats', 'mobile'])
const emit = defineEmits(['update:chat-active', 'create:chat', 'update:disk-import', 'export:chat'])

const chatOpened = computed(() => store.chatActive?.chatId != null)
const dynamicClass = computed(() => {
  return {
    'd-none': props.mobile && chatOpened.value,
    'd-flex': props.mobile && !chatOpened.value
  }
})

function emitThisChatActive(item: any) {
  emit('update:chat-active', item)
}

function emitCreateNewChat() {
  emit('create:chat')
}

function emitChatExport() {
  exitThisChat()
  emit('export:chat')
}

async function emitDiskImport() {
  store.loading = true
  await useFetch(useRuntimeConfig().public.api.importFromDisk, {method: 'post'})
  store.loading = false
  emit('update:disk-import')
}

function exitThisChat() {
  store.chatExited()
}

</script>


<style>
#chat-list-area {
  background: linear-gradient(180deg, rgba(2, 6, 23, 0.98), rgba(15, 23, 42, 0.98));
  border-right: 1px solid var(--color-border);
}

.action-bar {
  background: linear-gradient(180deg, rgba(2, 6, 23, 0.98), rgba(15, 23, 42, 0.98));
  border-bottom: 1px solid var(--color-border);
  gap: 0.4rem;
}

.action-bar .btn-group {
  flex-wrap: wrap;
  gap: 0.4rem;
}

.action-btn {
  border-radius: var(--radius-pill);
  background: rgba(15, 23, 42, 0.2);
  border-color: var(--color-border-strong);
  color: var(--color-text);
}

.action-btn:hover:not(:disabled) {
  border-color: var(--color-accent-strong);
  background: rgba(59, 130, 246, 0.12);
}
</style>
