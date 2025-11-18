package dev.marcal.chatvault.api.web.exception

open class MessageParserException(
    message: String,
) : RuntimeException(message)

class AmbiguousDateException(
    message: String,
) : MessageParserException(message)
