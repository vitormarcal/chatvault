<template>
  <div id="navbar"
       class="sticky-top d-flex p-2 m-0 justify-content-between align-items-center"
       v-if="chatActive">
    <div :class="{ 'blur-sensitive': store.blurEnabled }" class="chat-info-header d-flex align-items-center">
      <a href="#" class="h2 nav-back" @click="exitThisChat">
        <rotable-arrow-icon/>
      </a>
      <a href="#" class="m-2 profile-link" @click="() => toggleOpenChatConfig()">
        <profile-image :id="store.chatActive.chatId"/>
      </a>
      <div class="d-flex flex-column" role="button" @click="() => toggleOpenChatConfig()">
        <div class="font-weight-bold" id="name">{{ store.chatActive.chatName }}
        </div>
        <span class="badge align-content-end bg-success message-count-badge">{{ t('messagesCount', { count: store.chatActive.msgCount }) }}</span>
        <div class="small d-flex" id="details">{{ t('lastMessageSent') }}
          <message-created-at :date="store.chatActive.msgCreatedAt"/>
        </div>
      </div>

    </div>
    <search-bar :chatId="store.chatActive.chatId" @search="handleSearch" />
    <icon-three-dots class="self more-actions" role="button" @click="() => toggleOpenChatConfig()"/>

    <!-- Date Picker Modal -->
    <date-picker-modal
      :is-open="store.calendarOpen"
      :message-statistics="store.messageStatistics"
      :current-calendar-month="store.currentCalendarMonth"
      :statistics-loading="store.statisticsLoading"
      :user-locale="store.userLocale"
      @close="handleCalendarClose"
      @select-date="handleCalendarDateSelect"
      @month-changed="handleCalendarMonthChange"
    />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import {useMainStore} from '~/store';
import { useUiText } from '~/composables/useUiText';

const store = useMainStore();
const { t } = useUiText();
const chatActive = computed(() => store.chatActive.chatId > 0);

function exitThisChat() {
  store.chatExited();
}

function toggleOpenChatConfig() {
  store.chatConfigOpen = !store.chatConfigOpen;
}

function handleSearch({ query, chatId }: { query: string; chatId: string | null }) {
  if (!chatId) return;

  if (query.trim()) {
    store.performSearch(query, Number(chatId));
    store.searchOpen = true;
  } else {
    store.closeSearch();
  }
}

function handleCalendarClose() {
  store.closeCalendar();
}

function handleCalendarDateSelect(dateStr: string) {
  store.closeCalendar();
  store.jumpToDate(store.chatActive.chatId, dateStr);
}

function handleCalendarMonthChange(date: Date) {
  store.setCalendarMonth(date);
  store.fetchMessageStatistics(store.chatActive.chatId, date);
}

</script>

<style scoped>
#navbar {
  background: linear-gradient(90deg, rgba(2, 6, 23, 0.98), rgba(15, 23, 42, 0.98));
  border-bottom: 1px solid var(--color-border);
  padding: 0.65rem 1rem;
  gap: 0.75rem;
}

.blur-sensitive {
  filter: blur(6px);
  transition: filter 0.3s ease-in-out;
}

#navbar:hover .blur-sensitive {
  filter: none;
}

.chat-info-header {
  color: var(--color-text);
  gap: 0.4rem;
}

.nav-back {
  border-radius: var(--radius-pill);
  padding: 0.2rem 0.35rem;
  transition: background-color 0.2s ease, transform 0.2s ease;
}

.nav-back:hover {
  background: rgba(148, 163, 184, 0.16);
  transform: translateY(-1px);
}

.chat-info-header a {
  color: inherit;
  text-decoration: none;
}

.profile-link {
  border-radius: var(--radius-pill);
  padding: 0.2rem;
  transition: background-color 0.2s ease;
}

.profile-link:hover {
  background: rgba(148, 163, 184, 0.16);
}

#name {
  font-weight: 600;
  color: var(--color-text);
}

#details {
  color: var(--color-text-muted);
  opacity: 0.8;
}

.message-count-badge {
  background: rgba(16, 185, 129, 0.2);
  color: #d1fae5;
  font-weight: 500;
}

.more-actions {
  color: var(--color-text);
  opacity: 0.9;
  transition: transform 0.2s ease, opacity 0.2s ease;
}

.more-actions:hover {
  transform: translateY(-1px);
  opacity: 1;
}
</style>
