// https://nuxt.com/docs/api/configuration/nuxt-config

const host = process.env.API_HOST || '/api'
export default defineNuxtConfig({
    devtools: {enabled: true},
    ssr: true,
    runtimeConfig: {
        public: {
            api: {
                listChats: `${host}/chats`,
                getMessagesByIdAndPage: `${host}/chats/:chatId?page=:page`,
                getAttachmentByChatIdAndMessageId: `${host}/chats/:chatId/messages/:messageId/attachment`,
                importChatById: `${host}/chats/:chatId/messages/import`
            }
        }
    },
    css: ["bootstrap/dist/css/bootstrap.min.css"],
})
