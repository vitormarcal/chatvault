<template>
  <img :src="url" alt="Profile Photo" class="img-fluid rounded-circle"
       style="height:50px; width: 50px" id="pic">
</template>

<script setup lang="ts">

const props = defineProps(['id'])

const url = computed(() => {
  console.log(props.id)
  if (props.id) {
    return useRuntimeConfig().public.api.getProfileImage.replace(":chatId", props.id.toString())
  } else {
    return '/default-avatar.png'
  }
})

</script>

<style scoped>

</style>
