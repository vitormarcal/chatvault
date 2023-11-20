package dev.marcal.chatvault.in_out_boundary.output.exceptions

open class MessageParserException(message: String) : RuntimeException(message)

class AmbiguousDateException(message: String) : MessageParserException(message)