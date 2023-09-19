package dev.marcal.chatvault.web

import dev.marcal.chatvault.service.MessageCreator
import dev.marcal.chatvault.in_out_boundary.input.NewMessageInput
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/messages")
class MessageController(
    private val messageCreator: MessageCreator
) {

    @PostMapping
    fun newMessage(@RequestBody input: NewMessageInput): ResponseEntity<Unit> {
        messageCreator.execute(input)
        return ResponseEntity.noContent().build()
    }
}