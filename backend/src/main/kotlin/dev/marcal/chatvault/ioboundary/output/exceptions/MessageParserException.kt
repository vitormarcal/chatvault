package dev.marcal.chatvault.ioboundary.output.exceptions

open class MessageParserException(
    message: String,
) : RuntimeException(message)

class AmbiguousDateException(
    message: String,
) : MessageParserException(message)
