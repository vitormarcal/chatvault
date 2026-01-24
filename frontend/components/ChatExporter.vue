<script setup lang="ts">
import {useMainStore} from "~/store";
import { useUiText } from "~/composables/useUiText";
const props = defineProps(['allowDownloadAll'])
const store = useMainStore()
const { t } = useUiText()
const emit = defineEmits(['exit:dialog'])

const linkDownload = computed(() => {
  if (props.allowDownloadAll) {
    return useRuntimeConfig().public.api.exportAllChats
  } else {
    return useRuntimeConfig().public.api.exportChatById.replace(":chatId", store.chatActive?.chatId?.toString())
  }
})

const chatName = computed(() => {
  if (props.allowDownloadAll) {
    return "all-chats.zip"
  } else {
    return store.chatActive.chatName + '.zip'
  }
})


function cancel() {
  emit('exit:dialog')
}

</script>

<template>
  <div class="m-auto col-md-3">
    <div class="form-control">
      <div class="form-group d-flex justify-content-center mb-3">
        <a class="d-block text-center"
           :href="linkDownload"
           :download="chatName"
        >
          {{ t('getEntireChat') }}
        </a>
      </div>

      <div class="btn-group" role="group">
        <button type="button" @click="cancel"
                class="btn btn-outline-secondary ml-2">{{ t('cancel') }}
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>

</style>
