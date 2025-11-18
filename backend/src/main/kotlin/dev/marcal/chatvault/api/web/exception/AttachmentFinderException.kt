package dev.marcal.chatvault.api.web.exception

open class AttachmentFinderException(
    message: String? = null,
    throwable: Throwable?,
) : RuntimeException(message, throwable)

class AttachmentNotFoundException(
    message: String? = "the message was not found",
    throwable: Throwable? = null,
) : AttachmentFinderException(message, throwable)
