<template>
  <div class="flex-grow-1 text-right">
    <div class="small"  :class="classObject">{{ dateObject.date }}</div>
  </div>
</template>

<script>


export default {
  props: ['date'],
  computed: {
    dateObject() {
      const now = new Date()
      const theDateMessage = new Date(this.date)

      const isTheSameYear = now.getFullYear() === theDateMessage.getFullYear()
      const isTheSameMonth = now.getMonth() === theDateMessage.getMonth()
      const isTheSameDay = now.getDate() === theDateMessage.getDate()

      if (isTheSameYear && isTheSameMonth && isTheSameDay) {

        return {
          date: theDateMessage.getHours().toString().padStart(2, '0') + ':' + theDateMessage.getMinutes().toString().padStart(2, '0'),
          today: true
        }
      } else {
        return {
          date: theDateMessage.getDate().toString().padStart(2, '0') + '/' + theDateMessage.getMonth().toString().padStart(2, '0'),
          today: false
        }
      }
    },
    classObject() {
      return {
        'font-weight-bold': this.dateObject.today
      }
    }
  },
  methods: {}
}

</script>

<style scoped>

</style>
