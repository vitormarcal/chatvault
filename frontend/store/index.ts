import {defineStore} from 'pinia'
import {type Attachment, AttachmentConstructor, type Chat, ChatMessage} from "~/types";

export const useMainStore = defineStore('main', () => {
    const messages = ref([] as ChatMessage[])
    const attachmentsInfo = ref([] as any)
    const chatActive = ref({} as Chat)
    const authorActive = ref('')
    const chatConfigOpen = ref(false)
    const nextPage = ref(0)
    const pageSize = ref(20)
    const reloadImageProfile = ref(false)
    const authors = computed(() => {
        return [...new Set(messages.value.map(it => it.author))].filter(it => !!it)
    })
    const attachments = computed<Attachment[]>(() => {
        return attachmentsInfo.value.map((it: any) => AttachmentConstructor(it.name, attachmentUrl(chatActive.value.chatId, it.id)))
    })

    function updateMessages(items: ChatMessage []) {
        messages.value = items
    }

    function toChatMessage(item: any): ChatMessage {
        return new ChatMessage(item, chatActive.value.chatId)
    }

    function clearMessages() {
        messages.value = []
        nextPage.value = 0
    }

    function chatExited() {
        chatActive.value = {} as Chat
        chatConfigOpen.value = false
        attachmentsInfo.value = []
        clearMessages()
    }

    async function openChat(chat: Chat) {
        chatActive.value = chat
        await findAttachmentsInfo()
    }

    async function findAttachmentsInfo() {
        const url = useRuntimeConfig().public.api.getAttachmentsInfoByChatId.replace(":chatId", chatActive.value.chatId.toString())
        attachmentsInfo.value = await $fetch(url)
    }

    function toNextPage() {
        nextPage.value += 1
    }

    function updatePageSize(value: number) {
        if (value == pageSize.value) return true
        if (!isNaN(value) && value >= 1 && value <= 2000) {
            clearMessages()
            pageSize.value = value
            return true
        } else {
            return false
        }
    }

    return {
        chatActive,
        messages,
        attachmentsInfo,
        attachments,
        chatConfigOpen,
        nextPage,
        pageSize,
        authorActive,
        authors,
        reloadImageProfile,
        updateMessages,
        clearMessages,
        toNextPage,
        updatePageSize,
        chatExited,
        openChat,
        toChatMessage
    }
})

function attachmentUrl(chatId: number, messageId: number): string {
    return useRuntimeConfig().public.api.getAttachmentByChatIdAndMessageId
        .replace(':chatId', chatId.toString())
        .replace(':messageId', messageId.toString())
}