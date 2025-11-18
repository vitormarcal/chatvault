package dev.marcal.chatvault.domain.exception

open class DomainException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)

class AmbiguousDateException(
    message: String,
) : DomainException(message)

class MessageParserException(
    message: String,
) : DomainException(message)
