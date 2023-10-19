// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
    devtools: {enabled: true},
    runtimeConfig: {
        public: {
            api: {
                listChats: 'http://localhost:8080/api/chats',
                getMessagesByIdAndPage: 'http://localhost:8080/api/chats/:chatId?page=:page',
                getAttachmentByChatIdAndMessageId: 'http://localhost:8080/api/chats/:chatId/messages/:messageId/attachment'
            }
        }
    },
    css: ["bootstrap/dist/css/bootstrap.min.css"],
})
