package dev.marcal.chatvault.application.archive

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/** Presentation only. All navigation and media links work without a web server. */
internal object ArchivePages {
    val filters =
        linkedMapOf(
            "all" to "Todos",
            "images" to "Imagens",
            "videos" to "Vídeos",
            "audio" to "Áudios",
            "documents" to "Documentos",
        )
    private val dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    private val timeFormat = DateTimeFormatter.ofPattern("HH:mm")

    fun html(value: String): String =
        value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")

    fun pageName(page: Long) = if (page == 1L) "index.html" else "page-$page.html"

    fun galleryName(
        filter: String,
        page: Long = 1,
    ): String {
        val base = if (filter == "all") "gallery" else "gallery-$filter"
        return base + (if (page == 1L) "" else "-$page") + ".html"
    }

    fun mediaType(
        file: String?,
        name: String,
    ): String =
        when ((file ?: name).substringAfterLast('.').lowercase()) {
            "jpg", "jpeg", "png", "gif", "webp" -> "images"
            "mp4", "webm", "mov" -> "videos"
            "mp3", "ogg", "wav", "m4a", "opus" -> "audio"
            else -> "documents"
        }

    fun document(
        title: String,
        prefix: String,
        body: String,
        chatId: Long? = null,
        view: String = "document",
    ): String =
        buildString {
            append("<!doctype html><html lang=\"pt-BR\"><head><meta charset=\"utf-8\">")
            append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">")
            append("<meta http-equiv=\"Content-Security-Policy\" content=\"default-src 'none'; ")
            append("style-src 'self'; script-src 'self'; img-src 'self'; media-src 'self'; base-uri 'none'; form-action 'none'\">")
            append("<title>${html(title)}</title><link rel=\"stylesheet\" href=\"${prefix}assets/styles.css\">")
            append("<script defer src=\"${prefix}assets/chats.js\"></script><script defer src=\"${prefix}assets/archive.js\"></script>")
            append("</head><body data-root=\"$prefix\" data-chat-id=\"${chatId ?: ""}\"><div class=\"archive-app\">")
            append("<aside class=\"chat-sidebar\" id=\"chat-sidebar\"><a class=\"brand\" href=\"${prefix}index.html\">")
            append("<span class=\"brand-mark\">CV</span><span>ChatVault<small>ACERVO OFFLINE</small></span></a>")
            append(
                "<label class=\"chat-search\" hidden>Buscar conversa<input type=\"search\" placeholder=\"Buscar conversa…\" autocomplete=\"off\"></label>",
            )
            append(
                "<nav class=\"conversation-list\" aria-label=\"Conversas\"><a href=\"${prefix}index.html\">Ver todas as conversas</a></nav>",
            )
            append("<footer class=\"sidebar-footer\"><span class=\"offline-dot\"></span> Disponível sem internet")
            append(
                "<a href=\"${prefix}report.html\">Relatório de integridade</a><a href=\"${prefix}files.html\">Todos os arquivos</a></footer></aside>",
            )
            append("<button hidden class=\"sidebar-toggle\" aria-controls=\"chat-sidebar\" aria-expanded=\"false\">☰ Conversas</button>")
            append("<main class=\"workspace $view-view\">$body</main></div></body></html>")
        }

    fun messages(
        chatId: Long,
        name: String,
        page: Long,
        pages: Long,
        messages: List<Map<String, Any?>>,
        profile: String?,
        preview: List<Map<String, Any?>>,
        attachmentCount: Long,
    ): String {
        val navigation = pagination(page, pages, ::pageName)
        val body =
            buildString {
                append("<section class=\"conversation\">")
                append(header(name, "Conversa arquivada", profile))
                append("<div class=\"conversation-toolbar\"><a href=\"../../index.html\">← Todas as conversas</a>")
                append("<a href=\"dates.html\">Datas</a><a href=\"gallery.html\">Galeria ($attachmentCount)</a>")
                append(
                    "<label class=\"author-control\" hidden>Eu sou <select aria-label=\"Seu participante nesta conversa\"><option value=\"\">Escolher participante</option></select></label></div>",
                )
                append(navigation)
                append("<div class=\"message-list\">")
                if (messages.isEmpty()) {
                    append(
                        "<div class=\"empty-state\"><h2>Conversa sem mensagens</h2><p>Os arquivos preservados continuam disponíveis no inventário.</p></div>",
                    )
                }
                var lastDate = ""
                for (message in messages) {
                    val date = message["createdAt"].toString()
                    if (date.substringBefore('T') != lastDate) {
                        lastDate = date.substringBefore('T')
                        append("<div class=\"day-divider\"><span>${displayDate(date)}</span></div>")
                    }
                    val author = message["author"].toString()
                    val system = message["authorType"] == "SYSTEM"
                    val style = if (system) "system" else "message"
                    append("<article id=\"message-${message["id"]}\" class=\"$style\" data-author=\"${html(author)}\">")
                    if (!system) {
                        append(
                            "<header class=\"message-author tone-${Math.floorMod(author.hashCode(), 6)}\">${html(author)}</header>",
                        )
                    }
                    append("<p class=\"text\">${html(message["content"].toString())}</p>")
                    append(messageAttachment(message))
                    append(
                        "<footer class=\"message-meta\"><a href=\"#message-${message["id"]}\" aria-label=\"Link da mensagem ${message["id"]}\">#${message["id"]}</a>",
                    )
                    append(
                        "<time datetime=\"${html(
                            date,
                        )}\" title=\"${html(date.replace('T', ' '))}\">${displayTime(date)}</time></footer></article>",
                    )
                }
                append(navigation)
                append(
                    "</div></section><aside class=\"media-panel\"><div class=\"panel-heading\"><h2>Galeria</h2><span>$attachmentCount</span></div>",
                )
                append("<p class=\"muted\">Imagens, vídeos, áudios e documentos desta conversa.</p>")
                append("<a class=\"button gallery-open\" href=\"gallery.html\">Abrir galeria completa ↗</a>")
                if (preview.isEmpty()) append("<p class=\"empty-note\">Nenhum anexo nesta conversa.</p>")
                append("<div class=\"gallery-grid preview-grid\">")
                preview.forEach { append(galleryCard(it)) }
                append("</div></aside>")
            }
        return document(name, "../../", body, chatId, "chat")
    }

    fun gallery(
        chatId: Long,
        name: String,
        filter: String,
        page: Long,
        pages: Long,
        entries: List<Map<String, Any?>>,
        counts: Map<String, Long>,
        profile: String?,
    ): String {
        val navigation = pagination(page, pages) { galleryName(filter, it) }
        val body =
            buildString {
                append(header(name, "Galeria · ${counts["all"] ?: 0} anexos", profile))
                append(
                    "<div class=\"conversation-toolbar\"><a href=\"index.html\">← Voltar à conversa</a><a href=\"../../index.html\">Todas as conversas</a></div>",
                )
                append("<nav class=\"filter-tabs\" aria-label=\"Filtrar anexos\">")
                filters.forEach { (key, label) ->
                    append("<a class=\"filter-tab${if (filter == key) " active" else ""}\" href=\"${galleryName(key)}\"")
                    if (filter == key) append(" aria-current=\"page\"")
                    append(">$label <span>${counts[key] ?: 0}</span></a>")
                }
                append("</nav>$navigation<div class=\"gallery-content\">")
                if (entries.isEmpty()) {
                    append(
                        "<div class=\"empty-state\"><h2>Nenhum anexo nesta categoria</h2><p>Escolha outro filtro ou volte à conversa.</p></div>",
                    )
                }
                append("<div class=\"gallery-grid\">")
                entries.forEach { append(galleryCard(it)) }
                append("</div>$navigation</div>")
            }
        return document("Galeria — $name", "../../", body, chatId, "gallery")
    }

    private fun header(
        name: String,
        subtitle: String,
        profile: String?,
    ): String =
        buildString {
            append("<header class=\"conversation-header\">")
            if (profile != null) {
                append("<img class=\"avatar\" src=\"../../$profile\" alt=\"Foto da conversa\">")
            } else {
                append("<span class=\"avatar avatar-placeholder\">${html(name.take(1).uppercase())}</span>")
            }
            append("<div><h1>${html(name)}</h1><p>$subtitle</p></div><span class=\"archive-badge\">ARQUIVADO</span></header>")
        }

    private fun messageAttachment(message: Map<String, Any?>): String {
        val name = message["attachmentName"] as String? ?: return ""
        val file = message["exportedFile"] as String? ?: return "<p class=\"warning\">Anexo ausente: ${html(name)}</p>"
        val url = "../../$file"
        val media =
            when (mediaType(file, name)) {
                "images" -> "<a class=\"message-image\" href=\"$url\" data-preview=\"images\" data-name=\"${html(
                    name,
                )}\"><img loading=\"lazy\" src=\"$url\" alt=\"${html(name)}\"></a>"
                "videos" -> "<video controls preload=\"none\" src=\"$url\"></video>"
                "audio" -> "<audio controls preload=\"none\" src=\"$url\"></audio>"
                else -> "<span class=\"file-symbol\">▤</span>"
            }
        return "<div class=\"attachment\">$media<a class=\"download-link\" href=\"$url\" download=\"${html(
            name,
        )}\">↓ ${html(name)}</a></div>"
    }

    private fun galleryCard(entry: Map<String, Any?>): String =
        buildString {
            val name = entry["attachmentName"].toString()
            val file = entry["exportedFile"] as String?
            val kind = mediaType(file, name)
            append("<div class=\"gallery-card\" data-kind=\"$kind\">")
            if (file == null) {
                append("<div class=\"media-thumbnail missing-media\"><span>!</span><small>Anexo ausente</small></div>")
            } else {
                val url = "../../$file"
                when (kind) {
                    "images" ->
                        append(
                            "<a class=\"media-thumbnail\" href=\"$url\" data-preview=\"images\" data-name=\"${html(
                                name,
                            )}\"><img loading=\"lazy\" src=\"$url\" alt=\"${html(name)}\"></a>",
                        )
                    "videos" ->
                        append(
                            "<a class=\"media-thumbnail video-thumbnail\" href=\"$url\" data-preview=\"videos\" data-name=\"${html(
                                name,
                            )}\"><span>▶</span><small>Vídeo</small></a>",
                        )
                    "audio" ->
                        append(
                            "<div class=\"media-thumbnail audio-thumbnail\"><span>♫</span><audio controls preload=\"none\" src=\"$url\"></audio></div>",
                        )
                    else ->
                        append(
                            "<a class=\"media-thumbnail file-thumbnail\" href=\"$url\" download=\"${html(
                                name,
                            )}\"><span>▤</span><small>Documento</small></a>",
                        )
                }
                append("<a class=\"download-link\" href=\"$url\" download=\"${html(name)}\">↓ Baixar arquivo</a>")
            }
            append("<div class=\"media-caption\"><strong title=\"${html(name)}\">${html(name)}</strong>")
            append("<span>${displayDate(entry["createdAt"].toString())}</span>")
            append("<a class=\"message-context\" href=\"${entry["htmlPage"]}#message-${entry["id"]}\">Ver mensagem ↗</a></div></div>")
        }

    private fun pagination(
        page: Long,
        pages: Long,
        path: (Long) -> String,
    ): String =
        buildString {
            append("<nav class=\"pagination\" aria-label=\"Paginação\">")
            if (page > 1) append("<a href=\"${path(page - 1)}\">← Anterior</a>")
            append("<span>Página $page de $pages</span>")
            if (page < pages) append("<a href=\"${path(page + 1)}\">Próxima →</a>")
            append("</nav>")
        }

    private fun displayDate(value: String) = runCatching { LocalDateTime.parse(value).format(dateFormat) }.getOrElse { html(value) }

    private fun displayTime(value: String) = runCatching { LocalDateTime.parse(value).format(timeFormat) }.getOrElse { html(value) }
}
