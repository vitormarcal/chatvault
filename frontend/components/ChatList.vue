<template>
    <div id="chat-list-area" class="col-12  col-md-3 flex-column overflow-auto h-100 p-0">
        <template v-for="item in chats">
          <chat-item :item="item" @update:chat-active="emitThisChatActive"></chat-item>
        </template>

    </div>
</template>
<script setup lang="ts">

defineProps(['chats'])
const emit = defineEmits(['update:chat-active'])
function emitThisChatActive(item: any) {
  emit('update:chat-active', item)
}

</script>


<style>

</style>
