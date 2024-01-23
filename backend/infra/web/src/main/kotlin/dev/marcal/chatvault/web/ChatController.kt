package dev.marcal.chatvault.web

import dev.marcal.chatvault.in_out_boundary.input.AttachmentCriteriaInput
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
import java.util.concurrent.TimeUnit

@RestController
@RequestMapping("/api/chats")
class ChatController(
        private val chatLister: ChatLister,
        private val messageFinderByChatId: MessageFinderByChatId,
        private val attachmentFinder: AttachmentFinder,
        private val chatNameUpdater: ChatNameUpdater,
        private val attachmentInfoFinderByChatId: AttachmentInfoFinderByChatId,
        private val profileImageManager: ProfileImageManager,
        private val chatDeleter: ChatDeleter
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

    @DeleteMapping("{chatId}")
    fun deleteChatAndAssets(
            @PathVariable("chatId") chatId: Long,
    ) {
        return chatDeleter.execute(chatId)

    }

    @PatchMapping("{chatId}/chatName/{chatName}")
    fun update(
            @PathVariable chatId: Long,
            @PathVariable chatName: String,
    ): ResponseEntity<String> {
        chatNameUpdater.execute(chatId, chatName)
        return ResponseEntity.noContent().build()
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