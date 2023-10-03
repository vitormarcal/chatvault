import chatService from "../services/chat.service";

export default ({$axios}, inject) => {
  const allMethods = {
    ...chatService($axios)
  }
  const methods = Object.keys(allMethods)
  methods.forEach((method) => {
    inject(method, allMethods[method])
  })
}
