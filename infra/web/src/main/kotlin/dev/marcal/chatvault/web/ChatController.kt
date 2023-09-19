package dev.marcal.chatvault.web

import dev.marcal.chatvault.service.ListChat
import dev.marcal.chatvault.in_out_boundary.output.ChatLastMessageOutput
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/chats")
class ChatController(
    private val listChat: ListChat
) {

    @GetMapping
    fun listChats(): List<ChatLastMessageOutput> {
        return listChat.execute()
    }
}