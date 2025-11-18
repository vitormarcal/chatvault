package dev.marcal.chatvault.api.web

import dev.marcal.chatvault.api.dto.input.FileTypeInputEnum
import dev.marcal.chatvault.application.chat.BucketDiskImportService
import dev.marcal.chatvault.application.chat.ChatExportService
import dev.marcal.chatvault.application.chat.ChatImportService
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.util.HtmlUtils
import java.util.UUID

@RestController
@RequestMapping("/api/chats")
class ChatImportExportController(
    private val chatFileImporter: ChatImportService,
    private val bucketDiskImporter: BucketDiskImportService,
    private val chatFileExporter: ChatExportService,
) {
    @PostMapping("disk-import")
    fun executeDiskImport() {
        bucketDiskImporter.execute()
    }

    @PostMapping("{chatId}/messages/import")
    fun importFileByChatId(
        @PathVariable chatId: Long,
        @RequestParam("file") file: MultipartFile,
    ): ResponseEntity<String> = importChat(chatId = chatId, file = file)

    @PostMapping("import/{chatName}")
    fun importChatByChatName(
        @PathVariable chatName: String,
        @RequestParam("file") file: MultipartFile,
    ): ResponseEntity<String> = importChat(chatName = chatName, file = file)

    @GetMapping("{chatId}/export")
    fun importChatByChatName(
        @PathVariable chatId: Long,
    ): ResponseEntity<Resource> {
        val resource = chatFileExporter.execute(chatId)
        val headers = HttpHeaders()
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=${resource.filename ?: UUID.randomUUID()}.zip")
        headers.contentType = MediaType.APPLICATION_OCTET_STREAM
        return ResponseEntity
            .ok()
            .headers(headers)
            .body(resource)
    }

    @GetMapping("/export/all")
    fun executeDiskExport(): ResponseEntity<Resource> {
        val resource = chatFileExporter.executeAll()
        val headers = HttpHeaders()
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=${resource.filename ?: UUID.randomUUID()}.zip")
        headers.contentType = MediaType.APPLICATION_OCTET_STREAM
        return ResponseEntity
            .ok()
            .headers(headers)
            .body(resource)
    }

    fun importChat(
        chatId: Long? = null,
        chatName: String? = null,
        file: MultipartFile,
    ): ResponseEntity<String> {
        if (file.isEmpty) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "This file cannot be imported. It is empty.")
        }

        val fileType = getFileType(file)

        importChat(chatId, file, fileType, chatName)
        return ResponseEntity.noContent().build()
    }

    private val zipMimeTypes =
        setOf(
            "application/zip",
            "application/x-zip-compressed",
            "application/octet-stream",
        )

    private fun getFileType(file: MultipartFile): FileTypeInputEnum {
        val fileType =
            when (file.contentType) {
                null -> throw ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "This file cannot be imported. Media type is required.",
                )

                "text/plain" -> FileTypeInputEnum.TEXT
                in zipMimeTypes -> FileTypeInputEnum.ZIP

                else -> throw ResponseStatusException(
                    HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                    "This file cannot be imported. Media type not supported ${HtmlUtils.htmlEscape(file.contentType!!)}.",
                )
            }
        return fileType
    }

    private fun importChat(
        chatId: Long?,
        file: MultipartFile,
        fileType: FileTypeInputEnum,
        chatName: String?,
    ) {
        chatId?.let {
            chatFileImporter.execute(
                chatId = it,
                inputStream = file.inputStream,
                fileType = fileType,
            )
        } ?: run {
            chatFileImporter.execute(
                chatName = chatName,
                inputStream = file.inputStream,
                fileType = fileType,
            )
        }
    }
}
