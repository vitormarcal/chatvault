package dev.marcal.chatvault.in_out_boundary.output.exceptions

open class AttachmentFinderException(message: String? = null, throwable: Throwable?): RuntimeException(message, throwable)

class AttachmentNotFoundException(message: String? = "the message was not found", throwable: Throwable? = null): AttachmentFinderException(message, throwable)