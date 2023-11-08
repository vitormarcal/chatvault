<template>
  <div class="attachment" @click="toggleModal">
    <img class="w-100 img-thumbnail" v-if="isImage" :src="attachment.url" :alt="attachment.name"/>
    <video class="w-100" v-else-if="isVideo" :src="attachment.url" controls></video>
    <audio class="w-100" v-else-if="isAudio" :src="attachment.url" controls></audio>
    <a class="w-100" v-else :download='attachment.name'
       :href="attachment.url">{{ attachment.name }}</a>


    <div class="modal d-flex" :class="modalClass" v-if="showMedia">
      <div class="modal-content d-flex align-items-center m-auto">
        <img class=" img-thumbnail" v-if="isImage" :src="attachment.url" :alt="attachment.name"/>
        <video class="w-50" v-else-if="isVideo" :src="attachment.url" controls></video>
        <audio class="w-50" v-else-if="isAudio" :src="attachment.url" controls></audio>
      </div>

    </div>
  </div>
</template>

<script setup lang="ts">
const props = defineProps(['attachment'])

const isImage = computed(() => props.attachment.type === 'IMAGE')
const isVideo = computed(() => props.attachment.type === 'VIDEO')
const isAudio = computed(() => props.attachment.type === 'AUDIO')
const isPDF = computed(() => props.attachment.type === 'PDF')

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
<style scoped>

</style>