package dev.marcal.chatvault.web

import dev.marcal.chatvault.in_out_boundary.output.ChatLastMessageOutput
import dev.marcal.chatvault.in_out_boundary.output.MessageOutput
import dev.marcal.chatvault.service.ChatLister
import dev.marcal.chatvault.service.MessageFinderByChatId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.SortDefault
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/chats")
class ChatController(
    private val chatLister: ChatLister,
    private val messageFinderByChatId: MessageFinderByChatId
) {

    @GetMapping
    fun listChats(): List<ChatLastMessageOutput> {
        return chatLister.execute()
    }

    @GetMapping("{chatId}")
    fun findChatMessages(
        @PathVariable("chatId") chatId: Long,
        @SortDefault(
            sort = ["createdAt", "id"],
            direction = Sort.Direction.DESC
        ) pageable: Pageable
    ): Page<MessageOutput> {
        return messageFinderByChatId.execute(
            chatId = chatId,
            pageable = pageable
        )
    }
}