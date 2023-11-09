<script setup lang="ts">
import {useMainStore} from "~/store";

const fileType = ref('ALL')
const attachments = computed(() => {
  if (fileType.value != 'ALL') {
    return store.attachments.filter(it => it.type == fileType.value)
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
      <li class="nav-item">
        <a class="nav-link active" aria-current="page" href="#" @click="fileType = 'ALL'">All</a>
      </li>
      <li class="nav-item">
        <a class="nav-link active" aria-current="page" href="#" @click="fileType = 'VIDEO'">Video Files</a>
      </li>
      <li class="nav-item">
        <a class="nav-link active" aria-current="page" href="#" @click="fileType = 'IMAGE'">Image Files</a>
      </li>
      <li class="nav-item">
        <a class="nav-link active" aria-current="page" href="#" @click="fileType = 'PDF'">Documents</a>
      </li>
      <li class="nav-item">
        <a class="nav-link active" aria-current="page" href="#" @click="fileType = 'AUDIO'">Audio files</a>
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