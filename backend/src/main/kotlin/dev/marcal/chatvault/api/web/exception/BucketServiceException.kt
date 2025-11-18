package dev.marcal.chatvault.api.web.exception

open class BucketServiceException(
    message: String?,
    throwable: Throwable?,
) : RuntimeException(message, throwable)

class BucketFileNotFoundException(
    message: String?,
    throwable: Throwable?,
) : BucketServiceException(message, throwable)
