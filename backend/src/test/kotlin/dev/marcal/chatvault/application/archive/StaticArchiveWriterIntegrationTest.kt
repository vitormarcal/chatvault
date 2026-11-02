package dev.marcal.chatvault.application.archive

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import org.junit.jupiter.api.io.TempDir
import org.springframework.jdbc.datasource.DriverManagerDataSource
import java.nio.file.Files
import java.nio.file.Path
import java.util.UUID
import java.util.zip.ZipFile

/** Run with CHATVAULT_TEST_POSTGRES_URL pointing to an expendable PostgreSQL database. */
@EnabledIfEnvironmentVariable(named = "CHATVAULT_TEST_POSTGRES_URL", matches = ".+")
class StaticArchiveWriterIntegrationTest {
    @TempDir lateinit var temporary: Path

    @Test
    fun `exports every message including empty chats and orphans with safe offline links`() {
        val schema = "archive_" + UUID.randomUUID().toString().replace("-", "")
        val base = System.getenv("CHATVAULT_TEST_POSTGRES_URL")
        val admin = DriverManagerDataSource(base, "postgres", "export_test")
        admin.connection.use { it.createStatement().use { st -> st.execute("CREATE SCHEMA $schema") } }
        try {
            val dataSource =
                DriverManagerDataSource(
                    base + (
                        if ('?' in
                            base
                        ) {
                            "&"
                        } else {
                            "?"
                        }
                    ) + "currentSchema=$schema",
                    "postgres",
                    "export_test",
                )
            dataSource.connection.use { c ->
                c.createStatement().use { st ->
                    st.execute("CREATE TABLE chat (id BIGINT PRIMARY KEY, name TEXT, bucket TEXT, external_id TEXT)")
                    st.execute(
                        "CREATE TABLE message (id BIGINT PRIMARY KEY, chat_id BIGINT, author TEXT, author_type TEXT, created_at TIMESTAMP, content TEXT, external_id TEXT, attachment_name TEXT, attachment_path TEXT)",
                    )
                    st.execute(
                        "INSERT INTO chat VALUES (1, '<script>Nome & família</script>', 'bucket', 'external-chat'), (2, 'Vazia', 'empty', null)",
                    )
                    st.execute(
                        "INSERT INTO message SELECT n, 1, 'Autor', 'USER', timestamp '2024-01-01 10:00:00', '<script>alert(1)</script>' || chr(10) || 'Olá', null, null, null FROM generate_series(1,1001) n",
                    )
                    st.execute("INSERT INTO message VALUES (2000,99,'','SYSTEM',timestamp '2024-01-02','Órfã',null,null,null)")
                    st.execute("UPDATE message SET attachment_name = 'foto #%.png', attachment_path = 'bucket/' WHERE id IN (1,2)")
                    st.execute("UPDATE message SET attachment_name = 'ausente.jpg', attachment_path = 'bucket/' WHERE id = 3")
                    st.execute("UPDATE message SET attachment_name = '../outside.txt', attachment_path = '../' WHERE id = 4")
                }
            }
            dataSource.connection.use { c ->
                c.createStatement().use { st ->
                    st.execute("UPDATE message SET attachment_path = '/bucket/' WHERE id = 2")
                    st.execute("UPDATE message SET attachment_name = 'foto #%.png', attachment_path = 'bucket/' WHERE id BETWEEN 5 AND 130")
                    st.execute("UPDATE message SET attachment_name = 'clip.mp4', attachment_path = 'bucket/' WHERE id = 131")
                    st.execute("UPDATE message SET attachment_name = 'voz.ogg', attachment_path = 'bucket/' WHERE id = 132")
                    st.execute("UPDATE message SET attachment_name = 'documento.pdf', attachment_path = 'bucket/' WHERE id = 133")
                    st.execute("UPDATE message SET attachment_name = 'não vinculado.html', attachment_path = 'bucket/' WHERE id = 134")
                }
            }
            val root = Files.createDirectory(temporary.resolve("source"))
            val bucket = Files.createDirectory(root.resolve("bucket"))
            Files.write(bucket.resolve("foto #%.png"), byteArrayOf(1, 2, 3))
            Files.writeString(bucket.resolve("não vinculado.html"), "<script>bad</script>")
            Files.writeString(bucket.resolve("original.txt"), "original")
            Files.write(bucket.resolve("clip.mp4"), byteArrayOf(4, 5, 6))
            Files.write(bucket.resolve("voz.ogg"), byteArrayOf(7, 8, 9))
            Files.writeString(bucket.resolve("documento.pdf"), "%PDF-1.4 test")
            Files.writeString(temporary.resolve("outside.txt"), "private")
            Files.createSymbolicLink(bucket.resolve("link"), temporary.resolve("outside.txt"))
            val output = temporary.resolve("site")
            val mapper = jacksonObjectMapper()
            val writer = StaticArchiveWriter(dataSource, mapper)
            val summary = writer.write(root, output) { _, _, _ -> }
            assertEquals(3, summary.chats)
            assertEquals(1002, summary.messages)
            assertEquals(6, summary.files)
            assertEquals(4, summary.warnings)
            val messages = mapper.readTree(output.resolve("chats/chat-1/messages.json").toFile())
            assertEquals(1001, messages.size())
            assertEquals(messages[0]["exportedFile"], messages[1]["exportedFile"])
            assertTrue(messages[2]["exportedFile"].isNull)
            val page = Files.readString(output.resolve("chats/chat-1/index.html"))
            assertFalse(page.contains("<script>"))
            assertTrue(page.contains("&lt;script&gt;"))
            assertTrue(page.contains("page-2.html"))
            assertTrue(Files.readString(output.resolve("chats/chat-1/page-3.html")).contains("message-1001"))
            assertTrue(Files.readString(output.resolve("chats/chat-2/index.html")).contains("sem mensagens"))
            assertEquals("external-chat", mapper.readTree(output.resolve("data/chats.json").toFile())[0]["externalId"].asText())
            assertTrue(page.contains("data-author=\"Autor\""))
            assertTrue(page.contains("gallery.html"))
            assertTrue(Files.isRegularFile(output.resolve("assets/archive.js")))
            assertTrue(Files.isRegularFile(output.resolve("assets/chats.js")))
            val gallery = Files.readString(output.resolve("chats/chat-1/gallery.html"))
            assertEquals(60, Regex("class=\"gallery-card\"").findAll(gallery).count())
            assertTrue(gallery.contains("index.html#message-1"))
            assertTrue(gallery.contains("gallery-images.html"))
            assertEquals(
                14,
                Regex("class=\"gallery-card\"").findAll(Files.readString(output.resolve("chats/chat-1/gallery-3.html"))).count(),
            )
            assertEquals(
                9,
                Regex("class=\"gallery-card\"").findAll(Files.readString(output.resolve("chats/chat-1/gallery-images-3.html"))).count(),
            )
            assertTrue(Files.readString(output.resolve("chats/chat-1/gallery-videos.html")).contains("data-preview=\"videos\""))
            assertTrue(Files.readString(output.resolve("chats/chat-1/gallery-audio.html")).contains("<audio controls"))
            assertTrue(Files.readString(output.resolve("chats/chat-1/gallery-documents.html")).contains("não vinculado.html"))
            assertTrue(Files.readString(output.resolve("chats/chat-2/gallery.html")).contains("Nenhum anexo"))
            val manifest = mapper.readTree(output.resolve("manifest.json").toFile())
            assertEquals("complete_with_warnings", manifest["status"].asText())
            assertTrue(manifest["files"].any { it["source"].asText().endsWith(".html") && it["target"].asText().endsWith(".bin") })
            val zip = temporary.resolve("archive.zip")
            writer.zip(output, zip)
            ZipFile(zip.toFile()).use {
                assertNotNull(it.getEntry("chatvault-export/index.html"))
                assertNotNull(it.getEntry("chatvault-export/chats/chat-1/page-3.html"))
            }
            dataSource.connection.use { c ->
                c.createStatement().use { st ->
                    st.executeQuery("SELECT COUNT(*) FROM message").use { rs ->
                        rs.next()
                        assertEquals(1002, rs.getLong(1))
                    }
                }
            }
        } finally {
            admin.connection.use { it.createStatement().use { st -> st.execute("DROP SCHEMA $schema CASCADE") } }
        }
    }
}
