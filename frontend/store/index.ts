import {defineStore} from 'pinia'

export const useMainStore = defineStore('main', () => {
    const messages = ref([] as ChatMessage[])
    const authorActive = ref('')
    const chatConfigOpen = ref(false)
    const authors = computed(() => {
        return [...new Set(messages.value.map(it => it.author))].filter(it => !!it)
    })

    function updateMessages(items: ChatMessage[]) {
        messages.value = items
    }

    return {
        messages,
        chatConfigOpen,
        authorActive,
        authors,
        updateMessages
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