<template>
  <div v-if="url" class="attachment">
    <a :download='name'
       :href="url">{{ name }}</a>
  </div>
</template>

<script setup lang="ts">
const getAttachmentApiUrl = useRuntimeConfig().public.api.getAttachment
const props = defineProps(['name', 'id'])
const url = computed(() =>  props.id > 0 ? getAttachmentApiUrl : null)
</script>
<style scoped>

</style>