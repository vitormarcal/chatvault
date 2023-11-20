package dev.marcal.chatvault.in_out_boundary.output.exceptions
open class BucketServiceException(message: String?, throwable: Throwable?): RuntimeException(message, throwable)

class BucketFileNotFoundException(message: String?, throwable: Throwable?): BucketServiceException(message, throwable)