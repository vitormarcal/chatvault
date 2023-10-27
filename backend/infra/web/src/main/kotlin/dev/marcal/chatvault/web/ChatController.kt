package dev.marcal.chatvault.web

import dev.marcal.chatvault.in_out_boundary.input.AttachmentCriteriaInput
import dev.marcal.chatvault.in_out_boundary.output.ChatLastMessageOutput
import dev.marcal.chatvault.in_out_boundary.output.MessageOutput
import dev.marcal.chatvault.service.*
import org.springframework.core.io.Resource
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.SortDefault
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.MediaTypeFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.util.HtmlUtils

@RestController
@RequestMapping("/api/chats")
class ChatController(
    private val chatLister: ChatLister,
    private val messageFinderByChatId: MessageFinderByChatId,
    private val chatFileImporter: ChatFileImporter,
    private val attachmentFinder: AttachmentFinder,
    private val bucketDiskImporter: BucketDiskImporter
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
    fun importFileByChatId(
        @PathVariable chatId: Long,
        @RequestParam("file") file: MultipartFile
    ): ResponseEntity<String> {
        return importChat(chatId = chatId, file = file)
    }

    @PostMapping("import/{chatName}")
    fun importChatByChatName(
        @PathVariable chatName: String,
        @RequestParam("file") file: MultipartFile
    ): ResponseEntity<String> {
        return importChat(chatName = chatName, file = file)
    }


    fun importChat(chatId: Long? = null, chatName: String? = null, file: MultipartFile): ResponseEntity<String> {
        if (file.isEmpty) {
            return ResponseEntity.badRequest().body("The file is empty")
        }

        val fileType = when (file.contentType) {
            null -> return ResponseEntity.badRequest().body("media type is required.")
            "text/plain" -> "text"
            "application/zip" -> "zip"
            else -> return ResponseEntity.badRequest()
                .body("media type not supported ${HtmlUtils.htmlEscape(file.contentType!!)}.")
        }

        importChat(chatId, file, fileType, chatName)
        return ResponseEntity.ok("The file was imported")
    }

    private fun importChat(
        chatId: Long?,
        file: MultipartFile,
        fileType: String,
        chatName: String?
    ) {
        chatId?.let {
            chatFileImporter.execute(
                chatId = it,
                inputStream = file.inputStream,
                fileType = fileType
            )
        } ?: run {
            chatFileImporter.execute(
                chatName = chatName,
                inputStream = file.inputStream,
                fileType = fileType
            )
        }
    }

    @PostMapping("disk-import")
    fun executeDiskImport() {
        bucketDiskImporter.execute()
    }


    @GetMapping("{chatId}/messages/{messageId}/attachment")
    fun downloadAttachment(
        @PathVariable("chatId") chatId: Long,
        @PathVariable("messageId") messageId: Long
    ): ResponseEntity<Resource> {
        val resource = attachmentFinder.execute(
            AttachmentCriteriaInput(
                chatId = chatId,
                messageId = messageId
            )
        )
        val contentType: MediaType = MediaTypeFactory.getMediaTypes(resource.filename).firstOrNull()
            ?: MediaType.APPLICATION_OCTET_STREAM

        return ResponseEntity.ok()
            .contentType(contentType)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${resource.filename}\"")
            .body(resource)
    }
}