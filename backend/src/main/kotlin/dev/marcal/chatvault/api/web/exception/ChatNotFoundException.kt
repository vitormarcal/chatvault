package dev.marcal.chatvault.api.web.exception

class ChatNotFoundException(
    message: String? = null,
    throwable: Throwable? = null,
) : RuntimeException(message, throwable)
