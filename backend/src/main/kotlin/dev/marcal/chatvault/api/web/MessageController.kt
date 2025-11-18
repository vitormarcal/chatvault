package dev.marcal.chatvault.api.web

import dev.marcal.chatvault.api.dto.input.NewMessageInput
import dev.marcal.chatvault.application.message.MessageCreatorService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/messages")
class MessageController(
    private val messageCreator: MessageCreatorService,
) {
    @PostMapping
    fun newMessage(
        @RequestBody input: NewMessageInput,
    ): ResponseEntity<Unit> {
        messageCreator.execute(input)
        return ResponseEntity.noContent().build()
    }
}
