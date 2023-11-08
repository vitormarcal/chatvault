import {defineStore} from 'pinia'

export const useMainStore = defineStore('main', () => {
    const messages = ref([] as ChatMessage[])
    const chatActive = ref({} as Chat)
    const authorActive = ref('')
    const chatConfigOpen = ref(false)
    const nextPage = ref(0)
    const pageSize = ref(20)
    const authors = computed(() => {
        return [...new Set(messages.value.map(it => it.author))].filter(it => !!it)
    })
    const attachmentMessages = computed(() => messages.value.filter(it => !!it.attachment))

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
        clearMessages()
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
        attachmentMessages,
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
        toChatMessage
    }
})

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
        this.attachment = this.createAttachment(data.attachmentName, url)
    }

    private createAttachment(attachmentName: string | undefined, url: string): Attachment | null {
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
}