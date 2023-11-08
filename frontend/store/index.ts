import {defineStore} from 'pinia'

export const useMainStore = defineStore('main', () => {
    const messages = ref([] as ChatMessage[])
    const authorActive = ref('')
    const chatConfigOpen = ref(false)
    const nextPage = ref(0)
    const pageSize = ref(20)
    const authors = computed(() => {
        return [...new Set(messages.value.map(it => it.author))].filter(it => !!it)
    })

    function updateMessages(items: ChatMessage[]) {
        messages.value = items
    }

    function clearMessages() {
        messages.value = []
        nextPage.value = 0
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
        messages,
        chatConfigOpen,
        nextPage,
        pageSize,
        authorActive,
        authors,
        updateMessages,
        clearMessages,
        toNextPage,
        updatePageSize
    }
})

interface ChatMessage {
    id: number,
    author: string
    authorType: string,
    content: string,
    attachmentName?: string,
    createdAt: string
}