import {defineStore} from 'pinia';
import {type Attachment, AttachmentConstructor, type Chat, ChatMessage} from "~/types";
import type { SupportedLocale } from '~/types/localization';
import type { MessageStatistics } from '~/types/calendar';

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
        searchResults: [] as ChatMessage[],
        searchOpen: false,
        highlightUntilDate: null as string | null,
        reloadImageProfile: false,
        blurEnabled: localStorage.getItem("blurEnabled") === 'true',
        userLocale: (localStorage.getItem("userLocale") || 'auto') as SupportedLocale | 'auto',
        messageStatistics: null as MessageStatistics | null,
        currentCalendarMonth: new Date(),
        statisticsLoading: false,
        calendarOpen: false,
    });

    watch(() => state.authorActive, (newValue) => {
        localStorage.setItem("authorActive", newValue || '');
    });

    watch(() => state.blurEnabled, (newValue) => {
        localStorage.setItem("blurEnabled", newValue.toString());
    });

    watch(() => state.userLocale, (newValue) => {
        localStorage.setItem("userLocale", newValue);
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

    function updateLocale(newLocale: SupportedLocale | 'auto') {
        state.userLocale = newLocale;
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

    async function performSearch(query: string, chatId: number) {
        if (!query.trim()) {
            state.searchResults = [];
            return;
        }

        state.loading = true;
        try {
            const url = useRuntimeConfig().public.api.getMessagesByIdAndPage
                .replace(":chatId", chatId.toString())
                .replace(":page", "0")
                .replace(":size", "50")
                .replace(":query", query);
            const response = await $fetch<any>(url);
            state.searchResults = response.content.map((item: any) => toChatMessage(item));
        } catch (error) {
            console.error("Search error:", error);
            state.searchResults = [];
        } finally {
            state.loading = false;
        }
    }

    function closeSearch() {
        state.searchOpen = false;
        state.searchResults = [];
    }

    async function jumpToDate(chatId: number, targetDate: string) {
        state.loading = true;
        state.highlightUntilDate = targetDate;
        try {
            const url = useRuntimeConfig().public.api.getMessagesByDate
                .replace(":chatId", chatId.toString())
                .replace(":date", targetDate)
                .replace(":pageSize", state.pageSize.toString());
            const response = await $fetch<any>(url);
            state.messages = response.content.map((item: any) => toChatMessage(item));
            state.nextPage = 1; // Mark that we've loaded from a specific date
            state.searchOpen = false;
        } catch (error) {
            console.error("Jump to date error:", error);
        } finally {
            state.loading = false;
        }
    }

    function clearHighlight() {
        state.highlightUntilDate = null;
    }

    async function fetchMessageStatistics(chatId: number, date: Date) {
        state.statisticsLoading = true;
        try {
            const year = date.getFullYear();
            const month = date.getMonth() + 1;
            const url = useRuntimeConfig().public.api.getMessageStatistics
                .replace(":chatId", chatId.toString())
                .replace(":year", year.toString())
                .replace(":month", month.toString());
            const response = await $fetch<MessageStatistics>(url);
            state.messageStatistics = response;
        } catch (error) {
            console.error("Fetch message statistics error:", error);
            state.messageStatistics = null;
        } finally {
            state.statisticsLoading = false;
        }
    }

    function setCalendarMonth(date: Date) {
        state.currentCalendarMonth = new Date(date);
    }

    function openCalendar() {
        state.calendarOpen = true;
    }

    function closeCalendar() {
        state.calendarOpen = false;
    }

    return {
        ...toRefs(state),
        authors,
        attachments,
        toggleBlur,
        updateLocale,
        moreMessagesPath,
        updateMessages,
        clearMessages,
        toNextPage,
        updatePageSize,
        chatExited,
        openChat,
        toChatMessage,
        performSearch,
        closeSearch,
        jumpToDate,
        clearHighlight,
        fetchMessageStatistics,
        setCalendarMonth,
        openCalendar,
        closeCalendar,
    };
});

function attachmentUrl(chatId: number, messageId: number): string {
    return useRuntimeConfig().public.api.getAttachmentByChatIdAndMessageId
        .replace(':chatId', chatId.toString())
        .replace(':messageId', messageId.toString());
}
