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
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-arrow-left"
               viewBox="0 0 16 16">
            <path fill-rule="evenodd"
                  d="M15 8a.5.5 0 0 0-.5-.5H2.707l3.147-3.146a.5.5 0 1 0-.708-.708l-4 4a.5.5 0 0 0 0 .708l4 4a.5.5 0 0 0 .708-.708L2.707 8.5H14.5A.5.5 0 0 0 15 8z"/>
          </svg>
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

      <div class="chat-option mt-3 ">


        <button type="button" @click="toggleModal" class="btn btn-outline-primary btn-sm" data-bs-toggle="modal">
          Import/Export
        </button>

      </div>


    </div>
    <div id="infinite-list" class="message-list d-flex flex-column">
      <button v-if="hasNextPages" type="button" class="btn btn-light" @click="loadMoreMessages">Load more messages
      </button>
      <template v-for="(message, index) in messages" :key="index">
        <message-item :message="message" :chatId="chat.chatId"/>
      </template>
    </div>

    <div class="modal" :class="modalClass" tabindex="-1">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title text-black">File importer/exporter</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" @click="toggleModal"
                    aria-label="Close"></button>
          </div>
          <div class="modal-body">
            <div class="form-control">
              <div class="alert alert-success" v-if="importChatResult.data" role="alert">
                {{ importChatResult.data }}
              </div>
              <div class="alert alert-warning" v-if="importChatResult.error" role="alert">
                Failed to import, check server error logs
              </div>

              <div class="form-group d-flex justify-content-center mb-3">
                <a class="d-block text-center"
                   :href="downloadChatPath"
                   :download="chat.chatName + '.zip'"
                >
                  Get the entire chat
                </a>
              </div>

              <div class="form-group mb-3 pt-4 border-top border-2">
                <p class="text-black">Attention, these actions are related to the selected chat! </p>
                <label for="formFileSm" class="form-label text-black">Import messages to this chat</label>
                <input class="form-control form-control-sm"
                       @change="onFilePicked"
                       accept=".zip,.txt"
                       id="formFileSm"
                       ref="chatImportRef"
                       type="file">
              </div>
              <div class="btn-group" role="group">
                <button type="button" :disabled="disableUpload" @click="uploadFile"
                        class="btn btn-outline-secondary ml-2">Upload
                </button>
              </div>
            </div>


          </div>
          <div class="modal-footer">
            <button type="button" @click="toggleModal" class="btn btn-secondary" data-bs-dismiss="modal">Close</button>
          </div>
        </div>
      </div>
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
const chatImportRef = ref(null)
const clickModal = ref(false)
const importChatResult = ref({})
const disableUpload = ref(true)

const chatActive = computed(() => props.chat.chatId > 0)

const moreMessagesPath = computed(() =>
    useRuntimeConfig().public.api.getMessagesByIdAndPage.replace(":chatId", props.chat.chatId).replace(":page", store.nextPage).replace(":size", store.pageSize))

const importChatPath = computed(() => useRuntimeConfig().public.api.importChatById.replace(":chatId", props.chat.chatId))

const downloadChatPath = computed(() => useRuntimeConfig().public.api.exportChatById.replace(":chatId", props.chat.chatId))

const {data: response, refresh} = await useFetch(moreMessagesPath)

const content = computed(() => {
  return response?.value?.content ?? []
})

const authors = computed(() => {
  return store.authors
})

const messages = computed(() => store.messages)

const modalClass = computed(() => {
  return {
    'fade show d-block': !!clickModal.value
  }
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
    'd-none': props.mobile && (props.chat.chatId == null || store.chatConfigOpen),
    'col-md-6': !props.mobile && store.chatConfigOpen,
    'col-md-9': !props.mobile && !store.chatConfigOpen,
  }
})


function toggleModal() {
  clickModal.value = !clickModal.value
  importChatResult.value = {}
  if (chatImportRef.value) {
    chatImportRef.value.value = ''
  }
}

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

async function uploadFile() {
  if (chatImportRef?.value?.files && chatImportRef?.value?.files[0]) {
    let input = chatImportRef.value;
    const file = input.files[0]
    console.log(file)

    const form = new FormData()
    form.append("file", file);
    importChatResult.value = await useAsyncData(`upload ${file.name}`, () => $fetch(importChatPath.value, {
      method: "POST",
      body: form
    }))
    chatImportRef.value.value = ''
    if (importChatResult.value.data) {
      await refresh()
    }
  }
}

async function onFilePicked() {
  if (chatImportRef?.value?.files && chatImportRef?.value?.files[0]) {
    disableUpload.value = false
  }
}

watch(
    () => props.chat.chatId,
    (chatId) => {
      store.clearMessages()
      disableUpload.value = true
      if (chatImportRef.value) {
        chatImportRef.value.value = ''
      }
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