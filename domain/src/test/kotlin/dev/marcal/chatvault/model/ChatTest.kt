package dev.marcal.chatvault.model

import dev.marcal.chatvault.model.input.NewMessageInput
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class ChatTest {


    @Test
    fun `should add new message without attachment`() {
        Chat(id = 1L).acceptMessage(NewMessageInput("author name", createdAt = LocalDateTime.now(), content = "my message" ))
    }
}