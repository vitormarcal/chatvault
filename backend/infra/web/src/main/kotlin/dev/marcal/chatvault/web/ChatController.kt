package dev.marcal.chatvault.web

import dev.marcal.chatvault.in_out_boundary.input.AttachmentCriteriaInput
import dev.marcal.chatvault.in_out_boundary.input.FileTypeInputEnum
import dev.marcal.chatvault.in_out_boundary.output.AttachmentInfoOutput
import dev.marcal.chatvault.in_out_boundary.output.ChatLastMessageOutput
import dev.marcal.chatvault.in_out_boundary.output.MessageOutput
import dev.marcal.chatvault.service.*
import org.springframework.core.io.Resource
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.SortDefault
import org.springframework.http.*
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.util.HtmlUtils
import java.util.*
import java.util.concurrent.TimeUnit

@RestController
@RequestMapping("/api/chats")
class ChatController(
    private val chatLister: ChatLister,
    private val messageFinderByChatId: MessageFinderByChatId,
    private val chatFileImporter: ChatFileImporter,
    private val attachmentFinder: AttachmentFinder,
    private val bucketDiskImporter: BucketDiskImporter,
    private val chatFileExporter: ChatFileExporter,
    private val chatNameUpdater: ChatNameUpdater,
    private val attachmentInfoFinderByChatId: AttachmentInfoFinderByChatId,
    private val profileImageManager: ProfileImageManager
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

    @PatchMapping("{chatId}/chatName/{chatName}")
    fun update(
        @PathVariable chatId: Long,
        @PathVariable chatName: String,
    ): ResponseEntity<String> {
        chatNameUpdater.execute(chatId, chatName)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("import/{chatName}")
    fun importChatByChatName(
        @PathVariable chatName: String,
        @RequestParam("file") file: MultipartFile
    ): ResponseEntity<String> {
        return importChat(chatName = chatName, file = file)
    }

    @GetMapping("{chatId}/export")
    fun importChatByChatName(
        @PathVariable chatId: Long,
    ): ResponseEntity<Resource> {
        val resource = chatFileExporter.execute(chatId)
        val headers = HttpHeaders()
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=${UUID.randomUUID()}.zip")
        headers.contentType = MediaType.APPLICATION_OCTET_STREAM
        return ResponseEntity.ok()
            .headers(headers)
            .body(resource)
    }


    fun importChat(chatId: Long? = null, chatName: String? = null, file: MultipartFile): ResponseEntity<String> {
        if (file.isEmpty) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "This file cannot be imported. It is empty.")
        }

        val fileType = when (file.contentType) {
            null -> throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "This file cannot be imported. Media type is required."
            )

            "text/plain" -> FileTypeInputEnum.TEXT
            "application/zip" -> FileTypeInputEnum.ZIP
            else -> throw ResponseStatusException(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "This file cannot be imported. Media type not supported ${HtmlUtils.htmlEscape(file.contentType!!)}."
            )
        }

        importChat(chatId, file, fileType, chatName)
        return ResponseEntity.noContent().build()
    }

    private fun importChat(
        chatId: Long?,
        file: MultipartFile,
        fileType: FileTypeInputEnum,
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

        val cacheControl = CacheControl.maxAge(1, TimeUnit.DAYS)

        return ResponseEntity.ok()
            .contentType(resource.getMediaType())
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${resource.filename}\"")
            .cacheControl(cacheControl)
            .body(resource)
    }

    @GetMapping("{chatId}/attachments")
    fun downloadAttachment(
        @PathVariable("chatId") chatId: Long
    ): ResponseEntity<Sequence<AttachmentInfoOutput>> {
        val cacheControl = CacheControl.maxAge(5, TimeUnit.MINUTES)

        return ResponseEntity.ok()
            .cacheControl(cacheControl)
            .body(attachmentInfoFinderByChatId.execute(chatId))
    }

    @PostMapping("{chatId}/profile-image")
    fun putProfileImage(
        @PathVariable("chatId") chatId: Long,
        @RequestParam("profile-image") file: MultipartFile
    ): ResponseEntity<Any> {
        profileImageManager.updateImage(file.inputStream, chatId)

        return ResponseEntity.noContent().build()
    }

    @GetMapping("{chatId}/profile-image")
    fun getProfileImage(
        @PathVariable("chatId") chatId: Long
    ): ResponseEntity<Any> {
        val image = profileImageManager.getImage(chatId)

        val cacheControl = CacheControl.maxAge(5, TimeUnit.MINUTES)

        return ResponseEntity.ok()
            .contentType(image.getMediaType())
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${image.filename}\"")
            .cacheControl(cacheControl)
            .body(image)

    }
}

fun Resource.getMediaType(): MediaType {
    return MediaTypeFactory.getMediaTypes(this.filename).firstOrNull()
        ?: MediaType.APPLICATION_OCTET_STREAM
}