package dev.marcal.chatvault.application.archive

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.springframework.web.server.ResponseStatusException
import java.nio.file.Files
import java.nio.file.Path
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class ArchiveExportServiceTest {
    @TempDir lateinit var temporary: Path
    private val mapper = jacksonObjectMapper()

    @Test
    fun `job survives page closure and restart and allows repeated downloads`() {
        val writer = mockk<StaticArchiveWriter>()
        val gate = ArchiveGate()
        val entered = CountDownLatch(1)
        val release = CountDownLatch(1)
        every { writer.write(any(), any(), any()) } answers {
            entered.countDown()
            check(release.await(5, TimeUnit.SECONDS))
            StaticArchiveWriter.Summary(1, 2, 3, 1)
        }
        every { writer.zip(any(), any()) } answers {
            Files.writeString(secondArg(), "test zip")
            Unit
        }
        val source = Files.createDirectory(temporary.resolve("source"))
        val exports = temporary.resolve("exports")
        val service = ArchiveExportService(writer, gate, mapper, source.toString(), exports.toString())
        service.initialize()
        try {
            val job = service.start()
            assertTrue(entered.await(3, TimeUnit.SECONDS))
            assertThrows(ResponseStatusException::class.java) { service.start() }
            assertThrows(ResponseStatusException::class.java) { gate.beginMutation() }
            assertThrows(ResponseStatusException::class.java) { service.download(job.id) }
            release.countDown()
            awaitCompletion(service)
            assertEquals("completed_with_warnings", service.latest()!!.status)
            assertEquals(1L, service.latest()!!.warnings)
            repeat(2) { assertEquals("test zip", service.download(job.id).file.readText()) }
            val restarted = ArchiveExportService(writer, ArchiveGate(), mapper, source.toString(), exports.toString())
            try {
                restarted.initialize()
                assertEquals(job.id, restarted.latest()!!.id)
                assertEquals("test zip", restarted.download(job.id).file.readText())
            } finally {
                restarted.shutdown()
            }
        } finally {
            release.countDown()
            service.shutdown()
        }
    }

    @Test
    fun `failure releases mutations and never exposes partial zip`() {
        val writer = mockk<StaticArchiveWriter>()
        every { writer.write(any(), any(), any()) } throws IllegalStateException("read failed")
        val gate = ArchiveGate()
        val service =
            ArchiveExportService(writer, gate, mapper, temporary.resolve("source").toString(), temporary.resolve("exports").toString())
        service.initialize()
        try {
            val job = service.start()
            awaitCompletion(service)
            assertEquals("failed", service.latest()!!.status)
            assertThrows(ResponseStatusException::class.java) { service.download(job.id) }
            assertThrows(ResponseStatusException::class.java) { service.download("../bad") }
            gate.beginMutation()
            gate.endMutation()
        } finally {
            service.shutdown()
        }
    }

    @Test
    fun `interrupted export is reported after restart`() {
        val root = Files.createDirectories(temporary.resolve("exports/static-archives"))
        mapper.writeValue(
            root.resolve("latest.json").toFile(),
            ArchiveExportService.Job(UUID.randomUUID().toString(), "running", "Copiando"),
        )
        val service =
            ArchiveExportService(
                mockk(),
                ArchiveGate(),
                mapper,
                temporary.resolve("source").toString(),
                temporary.resolve("exports").toString(),
            )
        try {
            service.initialize()
            assertEquals("failed", service.latest()!!.status)
            assertEquals("Interrompida", service.latest()!!.phase)
        } finally {
            service.shutdown()
        }
    }

    private fun awaitCompletion(service: ArchiveExportService) {
        val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5)
        while (service.latest()?.status == "running" && System.nanoTime() < deadline) Thread.sleep(10)
        assertTrue(service.latest()?.status != "running")
        // State is persisted before the worker releases the gate.
        val state = temporary.resolve("exports/static-archives/latest.json")
        while (mapper.readTree(state.toFile())["status"].asText() == "running" && System.nanoTime() < deadline) Thread.sleep(10)
    }
}
