<script setup lang="ts">
import {useMainStore} from "~/store";

const gallery = ref({
  fileType: 'ALL',
  links: [
    {type: 'ALL', label: 'All'},
    {type: 'VIDEO', label: 'Video Files'},
    {type: 'IMAGE', label: 'Image Files'},
    {type: 'PDF', label: 'Documents'},
    {type: 'AUDIO', label: 'Audio files'},
  ]
})

const attachments = computed(() => {
  if (gallery.value.fileType != 'ALL') {
    return store.attachments.filter(it => it.type == gallery.value.fileType)
  } else {
    return store.attachments
  }
})

const store = useMainStore()

</script>

<template>
  <div>
    <slot></slot>
    <div class="title">Gallery</div>
    <ul class="nav justify-content-end">
      <li class="nav-item" v-for="item in gallery.links">
        <a class="nav-link active" aria-current="page" href="#" @click="gallery.fileType = item.type">{{
            item.label
          }}</a>
      </li>
    </ul>

    <div class="row">
      <div class="col-md-4" v-for="item in attachments" :key="item.url">
        <div class="card">
          <focusable-attachment :attachment="item"></focusable-attachment>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>

</style>