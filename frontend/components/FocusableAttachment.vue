<script setup lang="ts">
const props = defineProps(['attachment'])

let showMedia = ref(false);
const modalClass = computed(() => {
  return {
    'fade show d-block': !!showMedia.value
  }
})

function toggleModal() {
  showMedia.value = !showMedia.value
}
</script>

<template>
  <attachment :attachment="props.attachment" @click="toggleModal" role="button">
    <div class="modal d-flex" :class="modalClass" v-if="showMedia">
      <div class="modal-content d-flex align-items-center m-auto">
        <attachment :attachment="props.attachment"/>
      </div>
    </div>
  </attachment>

</template>

<style scoped>

</style>