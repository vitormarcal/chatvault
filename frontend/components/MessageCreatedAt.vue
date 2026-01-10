<template>
  <div class="flex-grow-1 text-end">
    <div class="small" :class="{ 'font-weight-bold': isToday }">{{ formattedDate }}</div>
  </div>
</template>

<script setup lang="ts">
import { useDateFormatting } from '~/composables/useDateFormatting'
import { isToday, formatDate as utilFormatDate } from '~/utils/dateFormatter'

const props = defineProps(['date'])

const { locale } = useDateFormatting()

const formattedDate = computed(() => {
  const result = utilFormatDate(props.date, locale.value)
  return result.formattedDate
})

const isToday = computed(() => {
  if (!props.date) return false
  const date = new Date(props.date)
  return isToday(date)
})

</script>

<style scoped>

</style>
