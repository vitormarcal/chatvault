package dev.marcal.chatvault.web

import dev.marcal.chatvault.service.ChatLegacyImporter
import dev.marcal.chatvault.service.NewMessage
import dev.marcal.chatvault.service.input.NewMessageInput
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/chat")
class ChatController(
    private val newMessage: NewMessage,
    private val chatLegacyImporter: ChatLegacyImporter
) {

    @PostMapping("messages")
    fun newMessage(input: NewMessageInput) {
        newMessage.execute(input)
    }

    @GetMapping("legacy/import")
    fun importLegacy() {
        chatLegacyImporter.execute()
    }

}