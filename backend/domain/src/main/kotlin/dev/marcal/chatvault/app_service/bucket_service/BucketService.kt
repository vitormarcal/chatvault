package dev.marcal.chatvault.app_service.bucket_service

import dev.marcal.chatvault.model.BucketFile
import org.springframework.core.io.Resource

interface BucketService {
    fun save(bucketFile: BucketFile)
    fun loadFileAsResource(bucketFile: BucketFile): Resource
    fun zipPendingImports(): Sequence<Resource>
    fun deleteZipImported(filename: String)
    fun saveToImportDir(bucketFile: BucketFile)
}

class BucketServiceException(message: String?, throwable: Throwable?): RuntimeException(message, throwable)