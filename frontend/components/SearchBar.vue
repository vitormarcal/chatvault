<template>
  <div class="search-bar">
    <input
        type="text"
        class="form-control"
        placeholder="Search..."
        v-model="store.searchQuery"
        @input="onSearch"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from "vue";
import {useMainStore} from "~/store";
const store = useMainStore()
const props = defineProps({
  chatId: { type: [Number, null], default: null },
});
const emit = defineEmits(["search"]);

const searchQuery = ref("");

function onSearch() {
  store.clearMessages();
  emit("search", { query: searchQuery.value, chatId: props.chatId });
}

watch(() => props.chatId, () => (searchQuery.value = ""));
</script>

<style scoped>
.search-bar {
  width: 100%;
  max-width: 300px;
}
</style>
