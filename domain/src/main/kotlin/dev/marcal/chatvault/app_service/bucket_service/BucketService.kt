package dev.marcal.chatvault.app_service.bucket_service

import dev.marcal.chatvault.model.BucketFile

interface BucketService {
    fun save(bucketFile: BucketFile)
}

class BucketServiceException(message: String?, throwable: Throwable?): RuntimeException(message, throwable)