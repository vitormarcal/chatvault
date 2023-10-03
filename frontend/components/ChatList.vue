<template>
    <div id="chat-list-area" class="col-12  col-md-3 flex-column overflow-auto h-100 p-0">
        <template v-for="item in chats">
          <chat-item :item="item"></chat-item>
        </template>

    </div>
</template>
<script setup lang="ts">

defineProps(['chats'])

</script>


<style>

</style>
