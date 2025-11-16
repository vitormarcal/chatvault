package dev.marcal.chatvault.web

import dev.marcal.chatvault.ioboundary.input.AttachmentCriteriaInput
import dev.marcal.chatvault.ioboundary.output.AttachmentInfoOutput
import dev.marcal.chatvault.ioboundary.output.ChatLastMessageOutput
import dev.marcal.chatvault.ioboundary.output.MessageOutput
import dev.marcal.chatvault.service.AttachmentFinder
import dev.marcal.chatvault.service.AttachmentInfoFinderByChatId
import dev.marcal.chatvault.service.ChatDeleter
import dev.marcal.chatvault.service.ChatLister
import dev.marcal.chatvault.service.ChatNameUpdater
import dev.marcal.chatvault.service.MessageFinderByChatId
import dev.marcal.chatvault.service.ProfileImageManager
import org.springframework.core.io.Resource
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.SortDefault
import org.springframework.http.CacheControl
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.MediaTypeFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
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
    private val chatDeleter: ChatDeleter,
) {
    @GetMapping
    fun listChats(): List<ChatLastMessageOutput> = chatLister.execute()

    @GetMapping("{chatId}")
    fun findChatMessages(
        @PathVariable("chatId") chatId: Long,
        @RequestParam("query", required = false) query: String? = null,
        @SortDefault(
            sort = ["createdAt", "id"],
            direction = Sort.Direction.DESC,
        ) pageable: Pageable,
    ): Page<MessageOutput> =
        messageFinderByChatId.execute(
            chatId = chatId,
            query = query,
            pageable = pageable,
        )

    @DeleteMapping("{chatId}")
    fun deleteChatAndAssets(
        @PathVariable("chatId") chatId: Long,
    ) = chatDeleter.execute(chatId)

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
        @PathVariable("messageId") messageId: Long,
    ): ResponseEntity<Resource> {
        val resource =
            attachmentFinder.execute(
                AttachmentCriteriaInput(
                    chatId = chatId,
                    messageId = messageId,
                ),
            )

        val cacheControl = CacheControl.maxAge(1, TimeUnit.DAYS)

        return ResponseEntity
            .ok()
            .contentType(resource.getMediaType())
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${resource.filename}\"")
            .cacheControl(cacheControl)
            .body(resource)
    }

    @GetMapping("{chatId}/attachments")
    fun downloadAttachment(
        @PathVariable("chatId") chatId: Long,
    ): ResponseEntity<Sequence<AttachmentInfoOutput>> {
        val cacheControl = CacheControl.maxAge(5, TimeUnit.MINUTES)

        return ResponseEntity
            .ok()
            .cacheControl(cacheControl)
            .body(attachmentInfoFinderByChatId.execute(chatId))
    }

    @PostMapping("{chatId}/profile-image")
    fun putProfileImage(
        @PathVariable("chatId") chatId: Long,
        @RequestParam("profile-image") file: MultipartFile,
    ): ResponseEntity<Any> {
        profileImageManager.updateImage(file.inputStream, chatId)

        return ResponseEntity.noContent().build()
    }

    @GetMapping("{chatId}/profile-image")
    fun getProfileImage(
        @PathVariable("chatId") chatId: Long,
    ): ResponseEntity<Any> {
        val image = profileImageManager.getImage(chatId)

        val cacheControl = CacheControl.maxAge(5, TimeUnit.MINUTES)

        return ResponseEntity
            .ok()
            .contentType(image.getMediaType())
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${image.filename}\"")
            .cacheControl(cacheControl)
            .body(image)
    }
}

fun Resource.getMediaType(): MediaType =
    MediaTypeFactory.getMediaTypes(this.filename).firstOrNull()
        ?: MediaType.APPLICATION_OCTET_STREAM
