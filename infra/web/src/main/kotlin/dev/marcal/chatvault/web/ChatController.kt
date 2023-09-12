package dev.marcal.chatvault.web

import dev.marcal.chatvault.service.legacy.ChatLegacyImporter
import dev.marcal.chatvault.service.NewMessage
import dev.marcal.chatvault.service.input.NewMessageInput
import dev.marcal.chatvault.service.legacy.EventSourceLegacyImporter
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/chat")
class ChatController(
    private val newMessage: NewMessage,
    private val chatLegacyImporter: ChatLegacyImporter,
    private val eventSourceLegacyImporter: EventSourceLegacyImporter
) {

    @PostMapping("messages")
    fun newMessage(input: NewMessageInput) {
        newMessage.execute(input)
    }

    @GetMapping("legacy/import")
    fun importLegacy() {
        chatLegacyImporter.importMessages()
    }

    @GetMapping("legacy/import/event-source")
    fun importLegacyEventSource() {
        eventSourceLegacyImporter.import()
    }

}