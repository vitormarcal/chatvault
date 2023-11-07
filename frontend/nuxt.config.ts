// https://nuxt.com/docs/api/configuration/nuxt-config

const host = process.env.API_HOST || '/api'
export default defineNuxtConfig({
    devtools: {enabled: true},
    ssr: false,
    runtimeConfig: {
        public: {
            api: {
                listChats: `${host}/chats`,
                getMessagesByIdAndPage: `${host}/chats/:chatId?page=:page&size=:size`,
                getAttachmentByChatIdAndMessageId: `${host}/chats/:chatId/messages/:messageId/attachment`,
                importChatById: `${host}/chats/:chatId/messages/import`,
                updateChatNameByChatId: `${host}/chats/:chatId/chatName/:chatName`,
                exportChatById: `${host}/chats/:chatId/export`,
                importChatByName: `${host}/chats/import/:chatName`,
                importFromDisk: `${host}/chats/disk-import`
            }
        }
    },
    css: ["bootstrap/dist/css/bootstrap.min.css"],
})
