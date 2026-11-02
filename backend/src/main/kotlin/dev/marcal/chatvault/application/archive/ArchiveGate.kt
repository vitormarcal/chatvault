package dev.marcal.chatvault.application.archive

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/** Coordinates all HTTP mutations and mail imports with the single export worker. */
class ArchiveGate {
    private val lock = ReentrantLock()
    private val available = lock.newCondition()
    private var exporting = false
    private var mutations = 0

    fun beginMutation() =
        lock.withLock {
            if (exporting) throw ResponseStatusException(HttpStatus.CONFLICT, "Exportação em andamento. Aguarde para alterar o acervo.")
            mutations++
        }

    fun endMutation() =
        lock.withLock {
            mutations--
            available.signalAll()
        }

    fun beginExport() =
        lock.withLock {
            if (exporting ||
                mutations > 0
            ) {
                throw ResponseStatusException(HttpStatus.CONFLICT, "Há uma exportação ou alteração em andamento. Tente novamente.")
            }
            exporting = true
        }

    fun endExport() =
        lock.withLock {
            exporting = false
            available.signalAll()
        }

    /** Mail stays queued in its handler instead of being rejected and potentially lost. */
    fun <T> awaitMutation(action: () -> T): T {
        lock.withLock {
            while (exporting) available.await()
            mutations++
        }
        try {
            return action()
        } finally {
            endMutation()
        }
    }
}
