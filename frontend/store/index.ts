import {defineStore} from 'pinia';
import {type Attachment, AttachmentConstructor, type Chat, ChatMessage} from "~/types";

export const useMainStore = defineStore('main', () => {
    const state = reactive({
        loading: false,
        messages: [] as ChatMessage[],
        attachmentsInfo: [] as any[],
        chatActive: {} as Chat,
        authorActive: localStorage.getItem("authorActive") || '',
        chatConfigOpen: false,
        nextPage: 0,
        pageSize: localStorage.getItem("pageSize") || 20,
        searchQuery: undefined,
        reloadImageProfile: false,
        blurEnabled: localStorage.getItem("blurEnabled") === 'true',
    });

    watch(() => state.authorActive, (newValue) => {
        localStorage.setItem("authorActive", newValue || '');
    });

    watch(() => state.blurEnabled, (newValue) => {
        localStorage.setItem("blurEnabled", newValue.toString());
    });

    const authors = computed(() => {
        return [...new Set(state.messages.map(it => it.author))].filter(Boolean);
    });

    const attachments = computed<Attachment[]>(() => {
        return state.attachmentsInfo.map((it: any) =>
            AttachmentConstructor(it.name, attachmentUrl(state.chatActive.chatId, it.id))
        );
    });

    const moreMessagesPath = computed(() => {
        return useRuntimeConfig().public.api.getMessagesByIdAndPage
            .replace(":chatId", state.chatActive.chatId?.toString())
            .replace(":page", state.nextPage.toString())
            .replace(":size", state.pageSize.toString())
            .replace(":query", state.searchQuery || "")
    });

    function toggleBlur() {
        state.blurEnabled = !state.blurEnabled;
    }

    function updateMessages(items: ChatMessage[]) {
        state.messages = items;
    }

    function toChatMessage(item: any): ChatMessage {
        return new ChatMessage(item, state.chatActive.chatId);
    }

    function clearMessages() {
        state.messages = [];
        state.nextPage = 0;
    }

    function chatExited() {
        state.chatActive = {} as Chat;
        state.chatConfigOpen = false;
        state.attachmentsInfo = [];
        clearMessages();
    }

    async function openChat(chat: Chat) {
        state.chatActive = chat;
        await findAttachmentsInfo();
    }

    async function findAttachmentsInfo() {
        const url = useRuntimeConfig().public.api.getAttachmentsInfoByChatId
            .replace(":chatId", state.chatActive.chatId.toString());
        state.attachmentsInfo = await $fetch(url);
    }

    function toNextPage() {
        state.nextPage += 1;
    }

    function updatePageSize(value: number): boolean {
        if (value === state.pageSize) return true;
        if (!isNaN(value) && value >= 1 && value <= 2000) {
            clearMessages();
            state.pageSize = value;
            return true;
        }
        return false;
    }

    return {
        ...toRefs(state),
        authors,
        attachments,
        toggleBlur,
        moreMessagesPath,
        updateMessages,
        clearMessages,
        toNextPage,
        updatePageSize,
        chatExited,
        openChat,
        toChatMessage,
    };
});

function attachmentUrl(chatId: number, messageId: number): string {
    return useRuntimeConfig().public.api.getAttachmentByChatIdAndMessageId
        .replace(':chatId', chatId.toString())
        .replace(':messageId', messageId.toString());
}
