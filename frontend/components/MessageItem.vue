<template>
  <div
      class="message-item rounded d-flex flex-column mt-3"
      :class="messageClasses"
      @click="handleClick"
  >
    <div v-if="enableJump" class="jump-hint">Clique para abrir contexto</div>
    <div class="message-id">{{ message.id }}</div>
    <div class="author font-weight-bold" :class="{ 'blur-sensitive': store.blurEnabled }">{{ message.author }}</div>
    <div
        class="message-content"
        v-html="formattedContent"
        :class="{ 'blur-sensitive': store.blurEnabled }"
    ></div>
    <focusable-attachment
        v-if="hasAttachment"
        :attachment="message.attachment"
        :class="{ 'blur-sensitive': store.blurEnabled }"
    />
    <div class="message-createdAt">{{ formattedDate }}</div>
  </div>
</template>

<script setup lang="ts">
import { useMainStore } from '~/store';
import { useDateFormatting } from '~/composables/useDateFormatting';

const store = useMainStore();
const { formatDate } = useDateFormatting();

const props = defineProps({
  message: Object,
  highlightUntilDate: [String, null],
  highlightMessageId: [Number, null],
  enableJump: { type: Boolean, default: false },
});
const emit = defineEmits(['jump']);

const isHighlightedByDate = ref(false);
const isHighlightedByMessage = ref(false);

const formattedContent = computed(() =>
    props.message.content.replace(
        /https?:\/\/[^\s]+/g,
        '<a href="$&" target="_blank" rel="noopener noreferrer">$&</a>'
    )
);

const formattedDate = computed(() => formatDate(props.message.createdAt));

const hasAttachment = computed(() => Boolean(props.message.attachment));

const isSystemMessage = computed(() => props.message.authorType === 'SYSTEM');

const isAuthorSelf = computed(() => props.message.author === store.authorActive);

const messageClasses = computed(() => ({
  'system-message w-50 align-self-center': isSystemMessage.value,
  'align-self-end': isAuthorSelf.value,
  'align-self-start': !isAuthorSelf.value,
  'highlighted': isHighlightedByDate.value || isHighlightedByMessage.value,
  'clickable-jump': props.enableJump,
}));

watch(
  () => props.highlightUntilDate,
  (newDate) => {
    if (newDate && props.message) {
      const messageDate = new Date(props.message.createdAt).toDateString();
      const highlightDate = new Date(newDate).toDateString();

      if (messageDate === highlightDate) {
        isHighlightedByDate.value = true;

        // Fade out highlight after 3 seconds
        setTimeout(() => {
          isHighlightedByDate.value = false;
          store.clearHighlight();
        }, 3000);
      }
    }
  },
  { immediate: true }
);

watch(
  () => props.highlightMessageId,
  (newId) => {
    if (!newId || !props.message) return;
    if (props.message.id === newId) {
      isHighlightedByMessage.value = true;

      setTimeout(() => {
        isHighlightedByMessage.value = false;
        store.clearHighlightMessage();
      }, 3000);
    }
  },
  { immediate: true }
);

function handleClick() {
  if (!props.enableJump || !props.message) return;
  emit('jump', { id: props.message.id, createdAt: props.message.createdAt });
}
</script>

<style scoped>
.message-item {
  padding: 0.65rem 0.75rem;
  border-radius: 14px;
  border: 1px solid var(--color-border);
  box-shadow: var(--shadow-sm);
  text-align: left;
  position: relative;
  background: linear-gradient(180deg, rgba(2, 6, 23, 0.95), rgba(2, 6, 23, 0.9));
}

.message-item.clickable-jump {
  cursor: pointer;
  border-color: rgba(59, 130, 246, 0.6);
  box-shadow: 0 0 0 1px rgba(59, 130, 246, 0.3), var(--shadow-sm);
}

.message-item.clickable-jump:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 18px rgba(15, 23, 42, 0.35);
}

.jump-hint {
  position: absolute;
  top: -0.6rem;
  left: 0.65rem;
  font-size: 0.62rem;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: #93c5fd;
  background: rgba(15, 23, 42, 0.9);
  border: 1px solid rgba(59, 130, 246, 0.4);
  padding: 0.2rem 0.45rem;
  border-radius: 999px;
  opacity: 0;
  transform: translateY(2px);
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.message-item.clickable-jump:hover .jump-hint {
  opacity: 1;
  transform: translateY(0);
}

.message-id {
  color: rgb(241, 241, 242);
  background-color: rgba(14, 97, 98, 0.9);
  position: absolute;
  font-size: 10px;
  padding-inline: 7px;
  border-radius: 50%;
  border: 1px solid rgba(0, 0, 0, 0.15);
  top: -0.5em;
  right: -0.5em;
  opacity: 0;
  transition: opacity 0.3s ease-in-out;
}

.message-item:hover .message-id {
  opacity: 1;
}

.author {
  font-weight: 600;
  color: var(--color-text);
  letter-spacing: -0.01em;
}

.message-content {
  overflow-wrap: break-word;
  word-break: break-word;
  white-space: pre-wrap;
  color: rgba(226, 232, 240, 0.9);
  line-height: 1.45;
  margin-top: 0.15rem;
}

.system-message .message-content {
  text-align: center;
}

.message-content :deep(a) {
  color: #7dd3fc;
  text-decoration: none;
  border-bottom: 1px solid rgba(125, 211, 252, 0.4);
}

.message-content :deep(a:hover) {
  color: #bae6fd;
  border-bottom-color: rgba(186, 230, 253, 0.8);
}

.message-createdAt {
  margin-left: 1rem;
  white-space: nowrap;
  font-size: 75%;
  opacity: 0.7;
  align-self: flex-end;
  color: var(--color-text-muted);
}

.blur-sensitive {
  filter: blur(6px);
  transition: filter 0.3s ease-in-out;
}

.message-item:hover .blur-sensitive {
  filter: none;
}

.message-item.highlighted {
  background: linear-gradient(90deg, rgba(30, 70, 32, 0.95) 0%, rgba(2, 6, 23, 0.98) 100%);
  animation: fadeHighlight 3s ease-out forwards;
}

@keyframes fadeHighlight {
  0% {
    background: linear-gradient(90deg, rgba(76, 175, 80, 0.95) 0%, rgba(2, 6, 23, 0.98) 100%);
    box-shadow: 0 0 10px rgba(76, 175, 80, 0.5);
  }
  100% {
    background: linear-gradient(90deg, rgba(30, 70, 32, 0.95) 0%, rgba(2, 6, 23, 0.98) 100%);
    box-shadow: var(--shadow-sm);
  }
}

.message-item :deep(.attachment) {
  margin-top: 0.65rem;
}
</style>
