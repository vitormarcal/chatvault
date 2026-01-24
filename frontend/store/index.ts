import {defineStore} from 'pinia';
import {type Attachment, AttachmentConstructor, type Chat, ChatMessage} from "~/types";
import type { SupportedLocale } from '~/types/localization';
import type { MessageStatistics } from '~/types/calendar';

export const useMainStore = defineStore('main', () => {
    const state = reactive({
        loading: false,
        messages: [] as ChatMessage[],
        contextMessages: [] as ChatMessage[],
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
        highlightMessageId: null as number | null,
        messageViewMode: 'default' as 'default' | 'search' | 'context',
        reloadImageProfile: false,
        blurEnabled: localStorage.getItem("blurEnabled") === 'true',
        userLocale: (localStorage.getItem("userLocale") || 'auto') as SupportedLocale | 'auto',
        messageStatistics: null as MessageStatistics | null,
        currentCalendarMonth: new Date(),
        statisticsLoading: false,
        calendarOpen: false,
        paginationMode: 'latest' as 'latest' | 'jump',
        jumpNextDate: null as string | null,
        jumpPrevDate: null as string | null,
        jumpHasMoreOlder: true,
        jumpHasMoreNewer: true,
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

    watch(() => state.searchQuery, (newQuery) => {
        if (newQuery && newQuery.trim()) {
            if (state.messageViewMode === 'context') {
                resetPaginationState();
            }
            state.messageViewMode = 'search';
        } else if (state.messageViewMode === 'search') {
            state.messageViewMode = 'default';
        }
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

    function updateContextMessages(items: ChatMessage[]) {
        state.contextMessages = items;
    }

    function toChatMessage(item: any): ChatMessage {
        return new ChatMessage(item, state.chatActive.chatId);
    }

    function clearMessages() {
        state.messages = [];
        state.nextPage = 0;
    }

    function clearContextMessages() {
        state.contextMessages = [];
    }

    function chatExited() {
        state.chatActive = {} as Chat;
        state.chatConfigOpen = false;
        state.attachmentsInfo = [];
        clearMessages();
        clearContextMessages();
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
            if (state.messageViewMode !== 'context') {
                state.messageViewMode = 'default';
            }
            return;
        }

        if (state.messageViewMode !== 'context') {
            state.messageViewMode = 'search';
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

    function clearSearchState() {
        state.searchQuery = '';
        state.searchResults = [];
        state.searchOpen = false;
    }

    async function jumpToDate(chatId: number, targetDate: string, messageId?: number) {
        state.loading = true;
        const dateOnly = normalizeDateOnly(targetDate);
        state.highlightUntilDate = dateOnly;
        state.highlightMessageId = messageId ?? null;
        state.paginationMode = 'jump';
        state.jumpHasMoreOlder = true;
        state.jumpHasMoreNewer = true;
        state.messageViewMode = 'context';
        try {
            const url = useRuntimeConfig().public.api.getMessagesByDate
                .replace(":chatId", chatId.toString())
                .replace(":date", dateOnly)
                .replace(":pageSize", state.pageSize.toString());
            const response = await $fetch<any>(url);
            const mapped = response.content.map((item: any) => toChatMessage(item));
            const sorted = sortMessagesAsc(mapped);
            state.contextMessages = sorted;
            const earliestDate = getEarliestDateOnly(state.contextMessages);
            const latestDate = getLatestDateOnly(state.contextMessages);
            state.jumpNextDate = earliestDate ? getPreviousDate(earliestDate) : null;
            state.jumpPrevDate = latestDate ? getNextDate(latestDate) : null;
            clearSearchState();
        } catch (error) {
            console.error("Jump to date error:", error);
        } finally {
            state.loading = false;
        }
    }

    async function loadMoreJumpMessages() {
        if (!state.jumpNextDate) {
            state.jumpHasMoreOlder = false;
            return;
        }

        if (!state.chatActive.chatId) {
            state.jumpHasMoreOlder = false;
            return;
        }

        const currentMessages = state.messageViewMode === 'context' ? state.contextMessages : state.messages;
        const earliestTimestamp = getEarliestTimestamp(currentMessages);
        if (earliestTimestamp == null) {
            state.jumpHasMoreOlder = false;
            return;
        }

        state.loading = true;
        try {
            let nextDate = state.jumpNextDate;
            let attempts = 0;
            let olderMessages: ChatMessage[] = [];

            while (attempts < 3) {
                const url = useRuntimeConfig().public.api.getMessagesByDate
                    .replace(":chatId", state.chatActive.chatId.toString())
                    .replace(":date", nextDate)
                    .replace(":pageSize", state.pageSize.toString());
                const response = await $fetch<any>(url);
                const fetched = response.content.map((item: any) => toChatMessage(item));

                if (fetched.length === 0) {
                    state.jumpHasMoreOlder = false;
                    break;
                }

                const older = fetched.filter((message) => {
                    return new Date(message.createdAt).getTime() < earliestTimestamp;
                });

                if (older.length > 0) {
                    olderMessages = older;
                    break;
                }

                nextDate = getPreviousDate(nextDate);
                attempts += 1;
            }

            if (olderMessages.length > 0) {
                const merged = mergeAndSortMessages(olderMessages, currentMessages);
                if (state.messageViewMode === 'context') {
                    state.contextMessages = merged;
                } else {
                    state.messages = merged;
                }
                const earliestDate = getEarliestDateOnly(merged);
                state.jumpNextDate = earliestDate ? getPreviousDate(earliestDate) : null;
            } else if (state.jumpHasMoreOlder) {
                state.jumpNextDate = nextDate;
            }
        } catch (error) {
            console.error("Load more (jump) error:", error);
        } finally {
            state.loading = false;
        }
    }

    async function loadMoreJumpMessagesNewer() {
        if (!state.jumpPrevDate) {
            state.jumpHasMoreNewer = false;
            return;
        }

        if (!state.chatActive.chatId) {
            state.jumpHasMoreNewer = false;
            return;
        }

        const currentMessages = state.messageViewMode === 'context' ? state.contextMessages : state.messages;
        const latestTimestamp = getLatestTimestamp(currentMessages);
        if (latestTimestamp == null) {
            state.jumpHasMoreNewer = false;
            return;
        }

        state.loading = true;
        try {
            let nextDate = state.jumpPrevDate;
            let attempts = 0;
            let newerMessages: ChatMessage[] = [];

            while (attempts < 3) {
                const url = useRuntimeConfig().public.api.getMessagesByDate
                    .replace(":chatId", state.chatActive.chatId.toString())
                    .replace(":date", nextDate)
                    .replace(":pageSize", state.pageSize.toString());
                const response = await $fetch<any>(url);
                const fetched = response.content.map((item: any) => toChatMessage(item));

                if (fetched.length === 0) {
                    state.jumpHasMoreNewer = false;
                    break;
                }

                const newer = fetched.filter((message) => {
                    return new Date(message.createdAt).getTime() > latestTimestamp;
                });

                if (newer.length > 0) {
                    newerMessages = newer;
                    break;
                }

                nextDate = getNextDate(nextDate);
                attempts += 1;
            }

            if (newerMessages.length > 0) {
                const merged = mergeAndSortMessages(newerMessages, currentMessages);
                if (state.messageViewMode === 'context') {
                    state.contextMessages = merged;
                } else {
                    state.messages = merged;
                }
                const latestDate = getLatestDateOnly(merged);
                state.jumpPrevDate = latestDate ? getNextDate(latestDate) : null;
            } else if (state.jumpHasMoreNewer) {
                state.jumpPrevDate = nextDate;
            }
        } catch (error) {
            console.error("Load newer (jump) error:", error);
        } finally {
            state.loading = false;
        }
    }

    async function loadOlderMessages() {
        if (state.paginationMode === 'jump') {
            await loadMoreJumpMessages();
            return;
        }

        toNextPage();
    }

    async function loadNewerMessages() {
        if (state.paginationMode !== 'jump') {
            return;
        }

        await loadMoreJumpMessagesNewer();
    }

    function resetPaginationState() {
        state.paginationMode = 'latest';
        state.jumpNextDate = null;
        state.jumpPrevDate = null;
        state.jumpHasMoreOlder = true;
        state.jumpHasMoreNewer = true;
        state.nextPage = 0;
        state.messageViewMode = 'default';
        state.highlightMessageId = null;
        clearContextMessages();
    }

    function clearHighlight() {
        state.highlightUntilDate = null;
    }

    function clearHighlightMessage() {
        state.highlightMessageId = null;
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
        updateContextMessages,
        clearMessages,
        clearContextMessages,
        toNextPage,
        updatePageSize,
        chatExited,
        openChat,
        toChatMessage,
        performSearch,
        closeSearch,
        clearSearchState,
        jumpToDate,
        clearHighlight,
        clearHighlightMessage,
        loadOlderMessages,
        loadNewerMessages,
        resetPaginationState,
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

function sortMessagesAsc(messages: ChatMessage[]): ChatMessage[] {
    return [...messages].sort((a, b) => {
        return new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime();
    });
}

function mergeAndSortMessages(olderMessages: ChatMessage[], existingMessages: ChatMessage[]): ChatMessage[] {
    const merged = new Map<number, ChatMessage>();
    for (const message of [...olderMessages, ...existingMessages]) {
        if (!merged.has(message.id)) {
            merged.set(message.id, message);
        }
    }
    return sortMessagesAsc(Array.from(merged.values()));
}

function getEarliestTimestamp(messages: ChatMessage[]): number | null {
    if (messages.length === 0) return null;
    return Math.min(...messages.map((message) => new Date(message.createdAt).getTime()));
}

function getLatestTimestamp(messages: ChatMessage[]): number | null {
    if (messages.length === 0) return null;
    return Math.max(...messages.map((message) => new Date(message.createdAt).getTime()));
}

function getEarliestDateOnly(messages: ChatMessage[]): string | null {
    const earliestTimestamp = getEarliestTimestamp(messages);
    if (earliestTimestamp == null) return null;
    return toDateOnlyString(new Date(earliestTimestamp));
}

function getLatestDateOnly(messages: ChatMessage[]): string | null {
    const latestTimestamp = getLatestTimestamp(messages);
    if (latestTimestamp == null) return null;
    return toDateOnlyString(new Date(latestTimestamp));
}

function toDateOnlyString(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
}

function normalizeDateOnly(dateStr: string): string {
    if (/^\d{4}-\d{2}-\d{2}$/.test(dateStr)) {
        return dateStr;
    }
    const parsed = new Date(dateStr);
    if (Number.isNaN(parsed.getTime())) {
        return dateStr;
    }
    return toDateOnlyString(parsed);
}

function getPreviousDate(dateStr: string): string {
    const date = new Date(`${dateStr}T00:00:00`);
    date.setDate(date.getDate() - 1);
    return toDateOnlyString(date);
}

function getNextDate(dateStr: string): string {
    const date = new Date(`${dateStr}T00:00:00`);
    date.setDate(date.getDate() + 1);
    return toDateOnlyString(date);
}
