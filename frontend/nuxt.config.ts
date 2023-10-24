// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
    devtools: {enabled: true},
    ssr: false,
    runtimeConfig: {
        public: {
            api: {
                listChats: '/api/chats',
                getMessagesByIdAndPage: '/api/chats/:chatId?page=:page',
                getAttachmentByChatIdAndMessageId: '/api/chats/:chatId/messages/:messageId/attachment'
            }
        }
    },
    css: ["bootstrap/dist/css/bootstrap.min.css"],
})
