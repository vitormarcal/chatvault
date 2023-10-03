<template>
    <div id="chat-item-area" class="chat-item d-flex flex-row p-2 w-100 border-bottom">
        <profile-image :id="item.chatId"/>
        <div class="w-50">
            <div class="name">{{ item.chatName }}</div>
            <div class="small text-truncate ">
                <i class="far fa-check-circle mr-1"></i>
                {{ item.content }}
            </div>
        </div>
        <message-created-at :date="item.msgCreatedAt"/>
    </div>
</template>
<script setup lang="ts">

defineProps(['item'])

</script>
<style>
.chat-item {
    cursor: pointer;
    background: white;
}

#chat-item-area {
  background: #00c58e;
}
</style>
