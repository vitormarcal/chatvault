<template>
  <div class="flex-grow-1 text-end">
    <div class="small" :class="classObject">{{ dateObject.date }}</div>
  </div>
</template>

<script setup lang="ts">

const props = defineProps(['date'])

const dateObject = computed(() => {
  const now = new Date()
  const theDateMessage = new Date(props.date)
  const isTheSameYear = now.getFullYear() === theDateMessage.getFullYear()
  const isTheSameMonth = now.getMonth() === theDateMessage.getMonth()
  const isTheSameDay = now.getDate() === theDateMessage.getDate()

  if (isTheSameYear && isTheSameMonth && isTheSameDay) {

    return {
      date: theDateMessage.getHours().toString().padStart(2, '0') + ':' + theDateMessage.getMinutes().toString().padStart(2, '0'),
      today: true
    }
  } else {
    return reactive({
      date: theDateMessage.getDate().toString().padStart(2, '0') + '/' + (theDateMessage.getMonth() + 1).toString().padStart(2, '0'),
      today: false
    })
  }
})

const classObject = computed(() => {
  return {
    'font-weight-bold': dateObject.value.today
  }
})

</script>

<style scoped>

</style>
