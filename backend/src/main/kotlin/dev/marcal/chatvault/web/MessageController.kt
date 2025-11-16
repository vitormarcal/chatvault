package dev.marcal.chatvault.web

import dev.marcal.chatvault.ioboundary.input.NewMessageInput
import dev.marcal.chatvault.service.MessageCreator
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/messages")
class MessageController(
    private val messageCreator: MessageCreator,
) {
    @PostMapping
    fun newMessage(
        @RequestBody input: NewMessageInput,
    ): ResponseEntity<Unit> {
        messageCreator.execute(input)
        return ResponseEntity.noContent().build()
    }
}
