package dev.marcal.chatvault.ioboundary.output.exceptions

open class BucketServiceException(
    message: String?,
    throwable: Throwable?,
) : RuntimeException(message, throwable)

class BucketFileNotFoundException(
    message: String?,
    throwable: Throwable?,
) : BucketServiceException(message, throwable)
