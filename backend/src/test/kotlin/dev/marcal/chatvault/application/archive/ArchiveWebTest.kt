package dev.marcal.chatvault.application.archive

import dev.marcal.chatvault.api.web.ArchiveExportController
import dev.marcal.chatvault.api.web.ArchiveMutationConfig
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@WebMvcTest(ArchiveExportController::class, ArchiveWebTest.MutationController::class)
@Import(ArchiveMutationConfig::class, ArchiveWebTest.MutationController::class)
class ArchiveWebTest {
    @Autowired lateinit var mvc: MockMvc

    @Autowired lateinit var gate: ArchiveGate

    @Autowired lateinit var service: ArchiveExportService

    @TestConfiguration
    class Config {
        @Bean fun archiveExportService() = mockk<ArchiveExportService>(relaxUnitFun = true)
    }

    @RestController
    class MutationController {
        @PostMapping("/api/test-mutation")
        fun mutation() = "updated"

        @GetMapping("/api/chats/{chatId}/export")
        fun legacyExport() = "legacy"

        @GetMapping("/api/test-read")
        fun read() = "read"
    }

    @Test
    fun `start returns accepted job and uncached status can be retrieved`() {
        val job = ArchiveExportService.Job(UUID.randomUUID().toString(), "running", "Preparando")
        every { service.start() } returns job
        every { service.latest() } returns job
        mvc.post("/api/archive-exports").andExpect {
            status { isAccepted() }
            header { string("Cache-Control", "no-store") }
            jsonPath("$.id") { value(job.id) }
        }
        mvc.get("/api/archive-exports/latest").andExpect {
            status { isOk() }
            jsonPath("$.status") { value("running") }
        }
    }

    @Test
    fun `empty status returns no content`() {
        every { service.latest() } returns null
        mvc.get("/api/archive-exports/latest").andExpect { status { isNoContent() } }
    }

    @Test
    fun `export blocks mutations and legacy export with path parameters while preserving reads`() {
        gate.beginExport()
        try {
            mvc.post("/api/test-mutation").andExpect { status { isConflict() } }
            mvc.get("/api/chats/1/export;parameter=value").andExpect { status { isConflict() } }
            mvc.get("/api/test-read").andExpect { status { isOk() } }
        } finally {
            gate.endExport()
        }
        mvc.post("/api/test-mutation").andExpect { status { isOk() } }
        // Completed HTTP mutations must release the gate.
        gate.beginExport()
        gate.endExport()
    }

    @Test
    fun `download is an uncached ZIP with attachment header`() {
        val id = UUID.randomUUID().toString()
        val file =
            java.nio.file.Files
                .createTempFile("archive-download-test", ".zip")
        try {
            java.nio.file.Files
                .writeString(file, "zip")
            every { service.download(id) } returns
                org.springframework.core.io
                    .FileSystemResource(file)
            val result =
                mvc
                    .get("/api/archive-exports/$id/download")
                    .andExpect {
                        status { isOk() }
                        content { contentType("application/zip") }
                        header { string("Cache-Control", "no-store") }
                        header { string("Content-Disposition", "attachment; filename=\"chatvault-html-$id.zip\"") }
                    }.andReturn()
            assertEquals("zip", result.response.contentAsString)
        } finally {
            java.nio.file.Files
                .deleteIfExists(file)
        }
    }
}
