package dev.marcal.chatvault.api.web

import dev.marcal.chatvault.application.archive.ArchiveExportService
import org.springframework.core.io.Resource
import org.springframework.http.CacheControl
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/archive-exports")
class ArchiveExportController(
    private val service: ArchiveExportService,
) {
    @PostMapping
    fun start(): ResponseEntity<ArchiveExportService.Job> =
        ResponseEntity.status(HttpStatus.ACCEPTED).cacheControl(CacheControl.noStore()).body(service.start())

    @GetMapping("/latest")
    fun latest(): ResponseEntity<ArchiveExportService.Job> =
        service.latest()?.let { ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(it) }
            ?: ResponseEntity.noContent().cacheControl(CacheControl.noStore()).build()

    @GetMapping("/{id}/download")
    fun download(
        @PathVariable id: String,
    ): ResponseEntity<Resource> =
        ResponseEntity
            .ok()
            .contentType(MediaType.parseMediaType("application/zip"))
            .cacheControl(CacheControl.noStore())
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"chatvault-html-$id.zip\"")
            .body(service.download(id))
}
