package dev.marcal.chatvault.ioboundary.output.exceptions

class ChatNotFoundException(
    message: String? = null,
    throwable: Throwable? = null,
) : RuntimeException(message, throwable)
