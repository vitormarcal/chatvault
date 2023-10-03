// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
    devtools: {enabled: true},
    runtimeConfig: {
        public: {
            api: {
                listChats: 'http://localhost:8080/api/chats'
            }
        }
    },
    css: ["bootstrap/dist/css/bootstrap.min.css"],
})
