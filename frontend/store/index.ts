import {defineStore} from 'pinia'

export const useMainStore = defineStore('main', () => {
    const messages = ref([] as ChatMessage[])
    const attachmentsInfo = ref([] as any)
    const chatActive = ref({} as Chat)
    const authorActive = ref('')
    const chatConfigOpen = ref(false)
    const nextPage = ref(0)
    const pageSize = ref(20)
    const authors = computed(() => {
        return [...new Set(messages.value.map(it => it.author))].filter(it => !!it)
    })
    const attachments = computed<Attachment[]>(() => {
        return attachmentsInfo.value.map((it: any) => createAttachment(it.name, attachmentUrl(chatActive.value.chatId, it.id)))
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

function createAttachment(attachmentName: string | undefined, url: string): Attachment | null {
    if (!attachmentName) {
        return null
    }

    if (/\.(jpg|jpeg|png|gif|webp)$/i.test(attachmentName)) {
        return {name: attachmentName, type: 'IMAGE', url: url}
    } else if (/\.(mp4|avi|mov)$/i.test(attachmentName)) {
        return {name: attachmentName, type: 'VIDEO', url: url}
    } else if (/\.(mp3|wav|opus)$/i.test(attachmentName)) {
        return {name: attachmentName, type: 'AUDIO', url: url}
    } else if (/\.(pdf)$/i.test(attachmentName)) {
        return {name: attachmentName, type: 'PDF', url: url}
    }

    return {name: attachmentName, type: 'UNKNOWN', url: url}
}

interface Chat {
    chatId: number
    chatName: string
    authorName: string
    authorType: string,
    content: string,
    msgCreatedAt: string
}

interface Attachment {
    name: string
    type: string
    url: string
}

export class ChatMessage {
    id: number
    chatId: number
    author: string
    authorType: string
    content: string
    attachment: Attachment | null
    createdAt: string

    constructor(data: any, chatId: number) {
        this.id = data.id
        this.chatId = chatId
        this.author = data.author
        this.authorType = data.authorType
        this.content = data.content
        this.createdAt = data.createdAt
        const url = useRuntimeConfig().public.api.getAttachmentByChatIdAndMessageId
            .replace(':chatId', this.chatId.toString())
            .replace(':messageId', this.id.toString())
        this.attachment = createAttachment(data.attachmentName, url)
    }
}