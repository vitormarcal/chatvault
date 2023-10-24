<template>
  <div id="chat-list-area"
       class="col-12  col-md-3 flex-column overflow-auto h-100 p-0"
       :class="dynamicClass">
    <template v-for="item in chats">
      <chat-item :item="item"
                 @update:chat-active="emitThisChatActive"
      />
    </template>

  </div>
</template>
<script setup lang="ts">

const props = defineProps(['chats', 'mobile', 'activeChat'])
const emit = defineEmits(['update:chat-active'])

const chatOpened = computed(() => props.activeChat?.chatId != null)
const dynamicClass = computed(() => {
  return {
    'd-none': props.mobile && chatOpened.value,
    'd-flex': props.mobile && !chatOpened.value
  }
})

function emitThisChatActive(item: any) {
  emit('update:chat-active', item)
}

</script>


<style>

</style>
