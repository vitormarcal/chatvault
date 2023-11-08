<script setup lang="ts">
import {useMainStore} from "~/store";
import Attachment from "~/components/Attachment.vue";

const messages = computed(() => store.attachmentMessages)

const store = useMainStore()

</script>

<template>
  <div>
    <slot></slot>
    <div class="title">Gallery</div>
    <ul class="nav justify-content-end">
      <li class="nav-item">
        <a class="nav-link active" aria-current="page" href="#">All</a>
      </li>
      <li class="nav-item">
        <a class="nav-link active" aria-current="page" href="#">Media Files</a>
      </li>
      <li class="nav-item">
        <a class="nav-link active" aria-current="page" href="#">Documents</a>
      </li>
      <li class="nav-item">
        <a class="nav-link active" aria-current="page" href="#">Audio files</a>
      </li>
    </ul>

    <div class="row">
      <div class="col-md-4" v-for="item in messages" :key="item.id">
        <div class="card">
          <attachment :attachment="item.attachment"></attachment>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>

</style>