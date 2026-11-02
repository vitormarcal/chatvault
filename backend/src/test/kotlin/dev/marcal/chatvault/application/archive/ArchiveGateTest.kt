package dev.marcal.chatvault.application.archive

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.web.server.ResponseStatusException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class ArchiveGateTest {
    @Test
    fun `export excludes mutations and a second export and releases after completion`() {
        val gate = ArchiveGate()
        gate.beginMutation()
        assertThrows(ResponseStatusException::class.java) { gate.beginExport() }
        gate.endMutation()
        gate.beginExport()
        assertThrows(ResponseStatusException::class.java) { gate.beginMutation() }
        assertThrows(ResponseStatusException::class.java) { gate.beginExport() }
        gate.endExport()
        gate.beginMutation()
        gate.endMutation()
    }

    @Test
    fun `mail waits until export finishes and releases reservation on failure`() {
        val gate = ArchiveGate()
        val entered = CountDownLatch(1)
        val worker = Executors.newSingleThreadExecutor()
        try {
            gate.beginExport()
            val future =
                worker.submit {
                    gate.awaitMutation {
                        entered.countDown()
                        error("mail failure")
                    }
                }
            assertFalse(entered.await(100, TimeUnit.MILLISECONDS))
            gate.endExport()
            assertTrue(entered.await(3, TimeUnit.SECONDS))
            assertThrows(java.util.concurrent.ExecutionException::class.java) { future.get(3, TimeUnit.SECONDS) }
            gate.beginExport()
            gate.endExport()
        } finally {
            worker.shutdownNow()
        }
    }
}
