package dev.marcal.chatvault.ioboundary.output.exceptions

class ChatImporterException(
    message: String? = null,
    throwable: Throwable? = null,
) : RuntimeException(message, throwable)
