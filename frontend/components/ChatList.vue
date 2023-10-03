<template>
    <div id="chat-list-area" class="col-12  col-md-4 flex-column overflow-auto h-100 p-0">
        <template v-for="item in chats">
            <ChatItem :item="item"
                      />
        </template>

    </div>
</template>
<script>

import ChatItem from "./ChatItem.vue";

export default {
  components: {ChatItem},
    props: ['chats'],
    methods: {

    }
}

</script>

<style>

</style>
