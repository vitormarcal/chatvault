const CHAT_SERVICE_URL = `/api/chats`
console.log(CHAT_SERVICE_URL)

export default ($axios) => {
  return  {
    getChatList: async (params) => {
      return (await $axios.get(CHAT_SERVICE_URL, {
        params
      })).data
    }
  }
}
