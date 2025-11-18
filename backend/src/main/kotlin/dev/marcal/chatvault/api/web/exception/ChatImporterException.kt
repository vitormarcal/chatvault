package dev.marcal.chatvault.api.web.exception

class ChatImporterException(
    message: String? = null,
    throwable: Throwable? = null,
) : RuntimeException(message, throwable)
