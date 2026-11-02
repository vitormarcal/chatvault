package dev.marcal.chatvault.application.archive

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.LinkOption.NOFOLLOW_LINKS
import java.nio.file.Path
import java.security.MessageDigest
import java.sql.Connection
import java.time.Instant
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.sql.DataSource

/** Reads the archive without invoking imports, deduplication or Hibernate writes. */
@Component
class StaticArchiveWriter(
    private val dataSource: DataSource,
    private val mapper: ObjectMapper,
) {
    data class Summary(
        val chats: Long,
        val messages: Long,
        val files: Long,
        val warnings: Long,
    )

    private data class Chat(
        val id: Long,
        val name: String,
        val bucket: String?,
        val recovered: Boolean = false,
        val externalId: String? = null,
    )

    private data class SavedFile(
        val source: String,
        val target: String,
        val size: Long,
        val sha256: String,
    )

    fun write(
        root: Path,
        destination: Path,
        progress: (String, Long, Long) -> Unit,
    ): Summary {
        val source = resolveArchivePath(root)
        val output = resolveArchivePath(destination)
        require(Files.isDirectory(source, NOFOLLOW_LINKS)) { "Diretório de arquivo não encontrado." }
        require(!output.startsWith(source) && !source.startsWith(output)) { "Origem e destino devem ser independentes." }
        Files.createDirectories(output.resolve("files"))
        Files.createDirectories(output.resolve("assets"))
        Files.writeString(output.resolve("assets/styles.css"), CSS)
        Files.writeString(output.resolve("assets/archive.js"), SCRIPT)
        val warnings = mutableListOf<String>()
        val copied = linkedMapOf<String, SavedFile>()
        // walk does not follow symbolic links. Unknown/active formats remain downloadable .bin files.
        Files.walk(source).use { paths ->
            paths.forEach { path ->
                val relative = source.relativize(path).toString().replace('\\', '/')
                when {
                    Files.isSymbolicLink(path) -> warnings.add("Link simbólico não copiado: $relative")
                    Files.isRegularFile(path, NOFOLLOW_LINKS) -> {
                        val extension =
                            path.fileName
                                .toString()
                                .substringAfterLast('.', "")
                                .lowercase()
                        val safeExtension = if (extension in SAFE_EXTENSIONS) extension else "bin"
                        val target = "files/file-${copied.size + 1}.$safeExtension"
                        val dest = output.resolve(target)
                        try {
                            Files.copy(path, dest)
                            val hash = hash(dest)
                            check(hash(path) == hash) { "Arquivo alterado durante a cópia: $relative" }
                            copied[relative] = SavedFile(relative, target, Files.size(dest), hash)
                            progress("Copiando arquivos", copied.size.toLong(), 0)
                        } catch (e: java.io.IOException) {
                            Files.deleteIfExists(dest)
                            warnings.add("Arquivo não copiado: $relative (${e.javaClass.simpleName})")
                        }
                    }
                    !Files.isDirectory(path, NOFOLLOW_LINKS) -> warnings.add("Arquivo especial não copiado: $relative")
                }
            }
        }
        val chatsJson = mutableListOf<Map<String, Any?>>()
        var totalMessages = 0L
        var chatCount = 0L
        dataSource.connection.use { connection ->
            connection.isReadOnly = true
            connection.transactionIsolation = Connection.TRANSACTION_REPEATABLE_READ
            connection.autoCommit = false
            try {
                val chats = readChats(connection)
                val expectedTotal =
                    connection.createStatement().use { st ->
                        st.executeQuery("SELECT COUNT(*) FROM message").use { rs ->
                            rs.next()
                            rs.getLong(1)
                        }
                    }
                for (chat in chats) {
                    val chatDir = output.resolve("chats/chat-${chat.id}")
                    Files.createDirectories(chatDir)
                    val count = countMessages(connection, chat.id)
                    val pages = maxOf(1L, (count + PAGE_SIZE - 1) / PAGE_SIZE)
                    val dates = linkedMapOf<String, Long>()
                    val authors = linkedSetOf<String>()
                    val attachmentCounts =
                        ArchivePages.filters.keys
                            .associateWith { 0L }
                            .toMutableMap()
                    val preview = mutableListOf<Map<String, Any?>>()
                    val profile = chat.bucket?.let { copied["${it.trim('/')}/profile-image.jpg"] }
                    if (chat.recovered) warnings.add("Mensagens recuperadas sem conversa correspondente: ${chat.id}")
                    mapper.factory
                        .createGenerator(
                            chatDir.resolve("messages.json").toFile(),
                            com.fasterxml.jackson.core.JsonEncoding.UTF8,
                        ).use { json ->
                            json.codec = mapper
                            json.writeStartArray()
                            val sql = "SELECT * FROM message WHERE chat_id = ? ORDER BY created_at, id"
                            connection.prepareStatement(sql).use { statement ->
                                statement.fetchSize = PAGE_SIZE
                                statement.setLong(1, chat.id)
                                statement.executeQuery().use { rows ->
                                    var page = 1L
                                    var written = 0L
                                    while (rows.next()) {
                                        val id = rows.getLong("id")
                                        val date = rows.getTimestamp("created_at").toLocalDateTime().toString()
                                        dates.putIfAbsent(date.substringBefore('T'), page)
                                        val attachmentName = rows.getString("attachment_name")
                                        val attachmentPath = rows.getString("attachment_path")
                                        val file = findAttachment(source, attachmentPath, attachmentName, copied)
                                        if (attachmentName != null &&
                                            file == null
                                        ) {
                                            warnings.add("Anexo ausente/inválido: conversa ${chat.id}, mensagem $id, $attachmentName")
                                        }
                                        val message =
                                            linkedMapOf<String, Any?>(
                                                "id" to id,
                                                "chatId" to chat.id,
                                                "author" to rows.getString("author"),
                                                "authorType" to rows.getString("author_type"),
                                                "createdAt" to date,
                                                "content" to rows.getString("content"),
                                                "externalId" to rows.getString("external_id"),
                                                "attachmentName" to attachmentName,
                                                "attachmentPath" to attachmentPath,
                                                "exportedFile" to file?.target,
                                                "htmlPage" to pageName(page),
                                            )
                                        json.writeObject(message)
                                        if (message["authorType"] != "SYSTEM") {
                                            message["author"]?.toString()?.takeIf { it.isNotBlank() }?.let { authors.add(it) }
                                        }
                                        if (attachmentName != null) {
                                            val type = ArchivePages.mediaType(file?.target, attachmentName)
                                            attachmentCounts["all"] = attachmentCounts.getValue("all") + 1
                                            attachmentCounts[type] = attachmentCounts.getValue(type) + 1
                                            if (preview.size < 6) preview.add(mediaEntry(message))
                                        }
                                        written++
                                        if (written % PAGE_SIZE == 0L) {
                                            page++
                                            progress("Exportando mensagens", totalMessages + written, expectedTotal)
                                        }
                                    }
                                    check(written == count) { "Contagem inconsistente na conversa ${chat.id}" }
                                    totalMessages += written
                                }
                            }
                            json.writeEndArray()
                        }
                    writeConversation(chatDir, chat, pages, profile?.target, preview, attachmentCounts)
                    val datesHtml =
                        dates.entries.joinToString(
                            "\n",
                        ) { (date, page) -> "<li><a href=\"${pageName(page)}\">${html(date)}</a></li>" }
                    Files.writeString(
                        chatDir.resolve("dates.html"),
                        document(
                            "Datas — ${chat.name}",
                            "../../",
                            "<a href=\"index.html\">Voltar à conversa</a><h1>Datas</h1><ul>$datesHtml</ul>",
                            chat.id,
                        ),
                    )
                    chatsJson.add(
                        linkedMapOf(
                            "id" to chat.id,
                            "name" to chat.name,
                            "bucket" to chat.bucket,
                            "externalId" to chat.externalId,
                            "recovered" to chat.recovered,
                            "messageCount" to count,
                            "pages" to pages,
                            "participants" to authors,
                            "profileFile" to profile?.target,
                            "attachmentCount" to attachmentCounts.getValue("all"),
                            "html" to "chats/chat-${chat.id}/index.html",
                        ),
                    )
                    chatCount++
                    progress("Exportando conversas", totalMessages, expectedTotal)
                }
                check(totalMessages == expectedTotal) { "Total de mensagens não corresponde ao banco." }
                connection.rollback()
            } catch (e: Exception) {
                connection.rollback()
                throw e
            }
        }
        Files.createDirectories(output.resolve("data"))
        mapper.writeValue(output.resolve("data/chats.json").toFile(), chatsJson)
        Files.writeString(output.resolve("assets/chats.js"), "globalThis.CHATVAULT_CHATS = " + mapper.writeValueAsString(chatsJson) + ";")
        val links =
            chatsJson.joinToString("\n") {
                "<li><a class=\"chat-card\" href=\"${it["html"]}\"><span class=\"avatar avatar-placeholder\">" +
                    html(it["name"].toString().take(1).uppercase()) + "</span><span><strong>" +
                    html(it["name"].toString()) +
                    "</strong><small>${it["messageCount"]} mensagens · ${it["attachmentCount"]} anexos</small></span></a></li>"
            }
        Files.writeString(
            output.resolve("index.html"),
            document(
                "ChatVault — Acervo",
                "",
                "<span class=\"eyebrow\">SEU HISTÓRICO, PRESERVADO</span><h1>Conversas arquivadas</h1>" +
                    "<p class=\"muted\">$chatCount conversas · $totalMessages mensagens · disponíveis offline</p>" +
                    "<ul class=\"chat-cards\">$links</ul>",
            ),
        )
        Files.writeString(
            output.resolve("files.html"),
            document(
                "Arquivos preservados",
                "",
                "<a href=\"index.html\">Início</a><h1>Todos os arquivos</h1><ul>" +
                    copied.values.joinToString("\n") {
                        "<li><a href=\"${it.target}\" download=\"${html(
                            it.source.substringAfterLast('/'),
                        )}\">${html(it.source)}</a> (${it.size} bytes)</li>"
                    } +
                    "</ul>",
            ),
        )
        val summary = Summary(chatCount, totalMessages, copied.size.toLong(), warnings.size.toLong())
        mapper.writeValue(
            output.resolve("manifest.json").toFile(),
            linkedMapOf(
                "formatVersion" to 1,
                "createdAt" to Instant.now().toString(),
                "status" to if (warnings.isEmpty()) "complete" else "complete_with_warnings",
                "summary" to summary,
                "files" to copied.values,
                "warnings" to warnings,
            ),
        )
        Files.writeString(
            output.resolve("report.html"),
            document(
                "Relatório de integridade",
                "",
                "<a href=\"index.html\">Início</a><h1>Relatório de integridade</h1>" +
                    "<p>${summary.chats} conversas, ${summary.messages} mensagens, " +
                    "${summary.files} arquivos. ${summary.warnings} pendências.</p>" +
                    "<p>Contagens de mensagens conferidas no snapshot do banco. " +
                    "Arquivos copiados conferidos por SHA-256. Datas locais preservadas sem conversão de fuso.</p>" +
                    "<p>Duplicatas existentes foram preservadas. JSON e manifesto acompanham este site. " +
                    "A busca do navegador se limita à página aberta.</p><ul>" +
                    warnings.joinToString("\n") { "<li>${html(it)}</li>" } +
                    "</ul>",
            ),
        )
        Files.writeString(
            output.resolve("README.txt"),
            "Extraia todo o ZIP e abra index.html no navegador. Não é necessário servidor ou internet.\nConsulte report.html e manifest.json antes de encerrar o ChatVault.\nJSON por conversa preserva os campos originais. Todos os arquivos copiados estão em files.html.\nA busca do navegador pesquisa apenas a página aberta. Alguns codecs exigem aplicativo externo.\nEste acervo contém dados pessoais. Guarde-o em local protegido e mantenha uma segunda cópia.\n",
        )
        progress("Verificando artefato", totalMessages, totalMessages)
        verify(output, copied.values.toList(), chatsJson, totalMessages)
        return summary
    }

    private fun findAttachment(
        source: Path,
        address: String?,
        name: String?,
        copied: Map<String, SavedFile>,
    ): SavedFile? {
        if (address == null || name == null) return null
        // Match BucketFile.file: Paths.get joins segments, including legacy leading slashes.
        val candidate =
            try {
                Path.of(source.toString(), address, name).normalize()
            } catch (_: java.nio.file.InvalidPathException) {
                return null
            }
        if (!candidate.startsWith(source)) return null
        return copied[source.relativize(candidate).toString().replace('\\', '/')]
    }

    private fun readChats(connection: Connection): List<Chat> {
        val result = mutableListOf<Chat>()
        connection.createStatement().use { st ->
            st.executeQuery("SELECT id, name, bucket, external_id FROM chat ORDER BY id").use { rs ->
                while (rs.next()) result.add(Chat(rs.getLong(1), rs.getString(2), rs.getString(3), externalId = rs.getString(4)))
            }
            st
                .executeQuery(
                    "SELECT DISTINCT m.chat_id FROM message m LEFT JOIN chat c ON c.id = m.chat_id WHERE c.id IS NULL ORDER BY m.chat_id",
                ).use { rs ->
                    while (rs.next()) result.add(Chat(rs.getLong(1), "Conversa recuperada ${rs.getLong(1)}", null, true))
                }
        }
        return result
    }

    private fun countMessages(
        connection: Connection,
        id: Long,
    ): Long =
        connection.prepareStatement("SELECT COUNT(*) FROM message WHERE chat_id = ?").use { st ->
            st.setLong(1, id)
            st.executeQuery().use { rs ->
                rs.next()
                rs.getLong(1)
            }
        }

    private fun mediaEntry(message: Map<String, Any?>): Map<String, Any?> =
        message.filterKeys { it in setOf("id", "attachmentName", "exportedFile", "createdAt", "htmlPage") }

    private fun writeConversation(
        dir: Path,
        chat: Chat,
        pages: Long,
        profile: String?,
        preview: List<Map<String, Any?>>,
        counts: Map<String, Long>,
    ) {
        val messages = mutableListOf<Map<String, Any?>>()
        val media = ArchivePages.filters.keys.associateWith { mutableListOf<Map<String, Any?>>() }
        val mediaPages =
            ArchivePages.filters.keys
                .associateWith { 1L }
                .toMutableMap()
        var page = 1L

        fun flushMessages() {
            Files.writeString(
                dir.resolve(pageName(page)),
                ArchivePages.messages(
                    chat.id,
                    chat.name,
                    page,
                    pages,
                    messages,
                    profile,
                    preview,
                    counts.getValue("all"),
                ),
            )
            messages.clear()
            page++
        }

        fun flushMedia(filter: String) {
            val galleryPage = mediaPages.getValue(filter)
            val totalPages = maxOf(1L, (counts.getValue(filter) + GALLERY_PAGE_SIZE - 1) / GALLERY_PAGE_SIZE)
            Files.writeString(
                dir.resolve(ArchivePages.galleryName(filter, galleryPage)),
                ArchivePages.gallery(
                    chat.id,
                    chat.name,
                    filter,
                    galleryPage,
                    totalPages,
                    media.getValue(filter),
                    counts,
                    profile,
                ),
            )
            media.getValue(filter).clear()
            mediaPages[filter] = galleryPage + 1
        }
        mapper.factory.createParser(dir.resolve("messages.json").toFile()).use { parser ->
            check(parser.nextToken() == com.fasterxml.jackson.core.JsonToken.START_ARRAY)
            while (parser.nextToken() == com.fasterxml.jackson.core.JsonToken.START_OBJECT) {
                val message = mapper.readValue(parser, object : TypeReference<Map<String, Any?>>() {})
                messages.add(message)
                if (messages.size == PAGE_SIZE) flushMessages()
                val name = message["attachmentName"] as String?
                if (name != null) {
                    val type = ArchivePages.mediaType(message["exportedFile"] as String?, name)
                    for (filter in listOf("all", type)) {
                        val entries = media.getValue(filter)
                        entries.add(mediaEntry(message))
                        if (entries.size == GALLERY_PAGE_SIZE) flushMedia(filter)
                    }
                }
            }
        }
        if (messages.isNotEmpty() || page == 1L) flushMessages()
        for (filter in ArchivePages.filters.keys) {
            if (media.getValue(filter).isNotEmpty() || mediaPages.getValue(filter) == 1L) flushMedia(filter)
        }
    }

    private fun verify(
        output: Path,
        files: List<SavedFile>,
        chats: List<Map<String, Any?>>,
        expected: Long,
    ) {
        for (file in files) check(hash(output.resolve(file.target)) == file.sha256) { "Hash divergente na saída." }
        var total = 0L
        for (chat in chats) {
            val dir = output.resolve("chats/chat-${chat["id"]}")
            var verified = 0L
            mapper.factory.createParser(dir.resolve("messages.json").toFile()).use { parser ->
                check(parser.nextToken() == com.fasterxml.jackson.core.JsonToken.START_ARRAY)
                for (page in 1L..(chat["pages"] as Long)) {
                    val content = Files.readString(dir.resolve(pageName(page)))
                    for (match in Regex("<article id=\"message-(-?[0-9]+)\"").findAll(content)) {
                        check(parser.nextToken() == com.fasterxml.jackson.core.JsonToken.START_OBJECT) { "JSON incompleto." }
                        val row = mapper.readTree<com.fasterxml.jackson.databind.JsonNode>(parser)
                        check(row["id"].asLong() == match.groupValues[1].toLong()) { "IDs divergentes entre JSON e HTML." }
                        verified++
                    }
                }
                check(parser.nextToken() == com.fasterxml.jackson.core.JsonToken.END_ARRAY) { "Mensagens ausentes no HTML." }
            }
            check(verified == chat["messageCount"]) { "Contagem divergente no artefato." }
            total += chat["messageCount"] as Long
        }
        check(total == expected)
        Files.walk(output).use { paths ->
            paths.filter { it.toString().endsWith(".html") }.forEach { page ->
                val content = Files.readString(page)
                for (match in Regex("(?:href|src)=\"([^\"]+)\"").findAll(content)) {
                    val relative = match.groupValues[1].substringBefore('#')
                    val target = if (relative.isEmpty()) page else page.parent.resolve(relative).normalize()
                    check(target.startsWith(output) && Files.isRegularFile(target)) { "Link local inválido." }
                }
            }
        }
    }

    fun zip(
        directory: Path,
        target: Path,
    ) {
        ZipOutputStream(Files.newOutputStream(target)).use { zip ->
            Files.walk(directory).use { paths ->
                paths.filter { Files.isRegularFile(it) }.forEach { path ->
                    zip.putNextEntry(ZipEntry("chatvault-export/" + directory.relativize(path).toString().replace('\\', '/')))
                    Files.copy(path, zip)
                    zip.closeEntry()
                }
            }
        }
    }

    private fun hash(path: Path): String {
        val digest = MessageDigest.getInstance("SHA-256")
        Files.newInputStream(path).use { stream ->
            val buffer = ByteArray(64 * 1024)
            var size = stream.read(buffer)
            while (size >=
                0
            ) {
                if (size > 0) digest.update(buffer, 0, size)
                size = stream.read(buffer)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    private fun pageName(page: Long) = if (page == 1L) "index.html" else "page-$page.html"

    private fun html(value: String) =
        value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")

    private fun document(
        title: String,
        prefix: String,
        body: String,
        chatId: Long? = null,
    ): String = ArchivePages.document(title, prefix, body, chatId)

    companion object {
        private const val PAGE_SIZE = 500
        private const val GALLERY_PAGE_SIZE = 60
        private val SAFE_EXTENSIONS =
            setOf("jpg", "jpeg", "png", "gif", "webp", "mp3", "ogg", "wav", "m4a", "opus", "mp4", "webm", "mov", "pdf", "txt", "zip")
        private val SCRIPT = requireNotNull(StaticArchiveWriter::class.java.getResource("/static-export/archive.js")).readText()
        private val CSS = requireNotNull(StaticArchiveWriter::class.java.getResource("/static-export/styles.css")).readText()
    }
}
