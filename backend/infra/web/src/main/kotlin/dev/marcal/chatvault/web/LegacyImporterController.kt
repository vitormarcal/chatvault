package dev.marcal.chatvault.web

import dev.marcal.chatvault.service.legacy.ChatLegacyImporter
import dev.marcal.chatvault.service.legacy.AttachmentLegacyImporter
import dev.marcal.chatvault.service.legacy.EventSourceLegacyImporter
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/legacy")
class LegacyImporterController(
    private val chatLegacyImporter: ChatLegacyImporter,
    private val eventSourceLegacyImporter: EventSourceLegacyImporter,
    private val attachmentLegacyImporter: AttachmentLegacyImporter
) {



    @GetMapping("import/event-source")
    fun importLegacy() {
        chatLegacyImporter.importToEventSource()
    }

    @GetMapping("legacy/import/event-source/messages")
    fun importLegacyEventSourceMessages() {
        eventSourceLegacyImporter.importMessagesFromEventSource()
    }

    @GetMapping("legacy/import/event-source/attachments")
    fun importLegacyEventSourceAttachments() {
        attachmentLegacyImporter.execute()
    }

}