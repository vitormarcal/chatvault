<template>
  <img :src="cachedUrl" alt="Profile Photo" class="img-fluid rounded-circle"
       style="height:50px; width: 50px" id="pic">
</template>

<script setup lang="ts">
import {useMainStore} from "~/store";

const store = useMainStore()
const props = defineProps(['id', 'urlProvided', 'cacheUrl'])
const key = ref(0)

const url = computed(() => {
  if (props.urlProvided) {
    return props.urlProvided
  } else if (props.id) {
    return useRuntimeConfig().public.api.getProfileImage.replace(":chatId", props.id.toString())
  } else {
    return '/default-avatar.png'
  }
})

const cachedUrl = computed(() => {
  const cacheUrl = props.cacheUrl !== undefined ? props.cacheUrl.value : true
  if (cacheUrl) {
    return url.value + `?cache=${key.value}`
  }
  return url.value

})

function forceUpdate() {
  key.value += 1
}

watch(
    () => store.reloadImageProfile,
    (reloadChatActive) => {
      if (reloadChatActive) {
        forceUpdate()
      }
    }
)

</script>

<style scoped>

</style>
