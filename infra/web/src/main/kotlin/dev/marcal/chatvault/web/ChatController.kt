package dev.marcal.chatvault.web

import dev.marcal.chatvault.in_out_boundary.output.ChatLastMessageOutput
import dev.marcal.chatvault.in_out_boundary.output.MessageOutput
import dev.marcal.chatvault.service.ChatFileImporter
import dev.marcal.chatvault.service.ChatLister
import dev.marcal.chatvault.service.MessageFinderByChatId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.SortDefault
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/chats")
class ChatController(
    private val chatLister: ChatLister,
    private val messageFinderByChatId: MessageFinderByChatId,
    private val chatFileImporter: ChatFileImporter
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

    @PostMapping("{chatId}/messages/import")
    fun importFile(@PathVariable chatId: Long, @RequestParam("file") file: MultipartFile): ResponseEntity<String> {
        if (file.isEmpty) {
            return ResponseEntity.badRequest().body("The file is empty")
        }

        val fileType = when(file.contentType) {
            null -> return ResponseEntity.badRequest().body("media type is required.")
            "text/plain" -> "text"
            "application/zip" -> "zip"
            else -> return ResponseEntity.badRequest().body("media type not supported ${file.contentType}.")
        }

        chatFileImporter.execute(
            chatId = chatId,
            inputStream = file.inputStream,
            fileType = fileType
        )

        return ResponseEntity.ok("The file was imported")

    }
}