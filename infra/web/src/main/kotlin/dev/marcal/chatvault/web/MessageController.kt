package dev.marcal.chatvault.web

import dev.marcal.chatvault.service.NewMessage
import dev.marcal.chatvault.service.input.NewMessageInput
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/messages")
class MessageController(
    private val newMessage: NewMessage
) {

    @PostMapping
    fun newMessage(@RequestBody input: NewMessageInput): ResponseEntity<Unit> {
        newMessage.execute(input)
        return ResponseEntity.noContent().build()
    }
}