package dev.marcal.chatvault.application.archive

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.FileSystemResource
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.time.Instant
import java.util.UUID
import java.util.concurrent.Executors

@Service
class ArchiveExportService(
    private val writer: StaticArchiveWriter,
    private val gate: ArchiveGate,
    private val mapper: ObjectMapper,
    @Value("\${chatvault.bucket.root}") private val sourceDirectory: String,
    @Value("\${chatvault.bucket.export}") private val exportDirectory: String,
) {
    data class Job(
        val id: String,
        val status: String,
        val phase: String,
        val processedMessages: Long = 0,
        val totalMessages: Long = 0,
        val copiedFiles: Long = 0,
        val warnings: Long = 0,
        val createdAt: String = Instant.now().toString(),
        val finishedAt: String? = null,
        val error: String? = null,
    )

    private val logger = LoggerFactory.getLogger(javaClass)
    private val executor =
        Executors.newSingleThreadExecutor { runnable ->
            Thread(runnable, "static-archive-export").apply { isDaemon = true }
        }
    private lateinit var jobsRoot: Path

    @Volatile private var current: Job? = null

    @PostConstruct
    fun initialize() {
        val source = resolveArchivePath(Path.of(sourceDirectory))
        val exportRoot = resolveArchivePath(Path.of(exportDirectory))
        jobsRoot = resolveArchivePath(exportRoot.resolve("static-archives"))
        require(!jobsRoot.startsWith(source) && !source.startsWith(jobsRoot)) {
            "O destino da exportação não pode sobrepor a origem."
        }
        Files.createDirectories(jobsRoot)
        val state = jobsRoot.resolve("latest.json")
        if (Files.isRegularFile(state)) {
            try {
                val restored = mapper.readValue(state.toFile(), Job::class.java)
                require(UUID.fromString(restored.id).toString() == restored.id)
                current =
                    if (restored.status ==
                        "running"
                    ) {
                        restored.copy(
                            status = "failed",
                            phase = "Interrompida",
                            error = "O serviço reiniciou durante a exportação. Inicie uma nova exportação.",
                            finishedAt = Instant.now().toString(),
                        )
                    } else {
                        restored
                    }
                persist()
            } catch (e: Exception) {
                logger.warn("Não foi possível recuperar o estado da exportação estática", e)
            }
        }
    }

    fun latest(): Job? = current

    @Synchronized
    fun start(): Job {
        gate.beginExport()
        try {
            val job = Job(UUID.randomUUID().toString(), "running", "Preparando")
            val directory = jobsRoot.resolve(job.id)
            Files.createDirectory(directory)
            current = job
            persist()
            executor.submit { generate(job, directory) }
            return job
        } catch (e: Exception) {
            gate.endExport()
            throw e
        }
    }

    private fun generate(
        job: Job,
        directory: Path,
    ) {
        try {
            val content = directory.resolve("site")
            Files.createDirectory(content)
            val summary =
                writer.write(Path.of(sourceDirectory), content) { phase, processed, total ->
                    val previous = current!!
                    current =
                        if (phase ==
                            "Copiando arquivos"
                        ) {
                            previous.copy(phase = phase, copiedFiles = processed)
                        } else {
                            previous.copy(phase = phase, processedMessages = processed, totalMessages = total)
                        }
                }
            current = current!!.copy(phase = "Compactando", warnings = summary.warnings)
            val temporary = directory.resolve("archive.zip.partial")
            writer.zip(content, temporary)
            Files.move(temporary, directory.resolve("archive.zip"), StandardCopyOption.ATOMIC_MOVE)
            current =
                current!!.copy(
                    status =
                        if (summary.warnings ==
                            0L
                        ) {
                            "completed"
                        } else {
                            "completed_with_warnings"
                        },
                    phase = "Concluída",
                    processedMessages = summary.messages,
                    totalMessages = summary.messages,
                    copiedFiles = summary.files,
                    warnings = summary.warnings,
                    finishedAt = Instant.now().toString(),
                )
            persist()
        } catch (e: Exception) {
            logger.error("Falha na exportação estática {}", job.id, e)
            current =
                current!!.copy(
                    status = "failed",
                    phase = "Falha",
                    finishedAt = Instant.now().toString(),
                    error =
                        "Não foi possível concluir a exportação. Verifique espaço em disco, permissões e os logs do servidor. " +
                            "Os dados originais foram preservados.",
                )
            try {
                persist()
            } catch (failure: Exception) {
                logger.error("Falha ao salvar estado da exportação", failure)
            }
        } finally {
            gate.endExport()
        }
    }

    private fun persist() {
        val temporary = jobsRoot.resolve("latest.json.tmp")
        mapper.writeValue(temporary.toFile(), current)
        Files.move(temporary, jobsRoot.resolve("latest.json"), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
    }

    fun download(id: String): FileSystemResource {
        if (runCatching { UUID.fromString(id).toString() }.getOrNull() != id) throw ResponseStatusException(HttpStatus.NOT_FOUND)
        val zip = jobsRoot.resolve(id).resolve("archive.zip")
        // A ZIP is made visible only after generation and verification finish.
        if (!Files.isRegularFile(zip)) throw ResponseStatusException(HttpStatus.NOT_FOUND, "Exportação ainda não disponível.")
        return FileSystemResource(zip)
    }

    @PreDestroy
    fun shutdown() {
        executor.shutdown()
    }
}

/** Canonicalizes existing parents before creating a destination, including symlinked parents. */
internal fun resolveArchivePath(path: Path): Path {
    val absolute = path.toAbsolutePath().normalize()
    var existing = absolute
    while (!Files.exists(existing)) {
        existing = requireNotNull(existing.parent) { "Não foi possível resolver o diretório." }
    }
    return existing.toRealPath().resolve(existing.relativize(absolute)).normalize()
}
