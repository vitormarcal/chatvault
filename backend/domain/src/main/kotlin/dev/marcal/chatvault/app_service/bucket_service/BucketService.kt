package dev.marcal.chatvault.app_service.bucket_service

import dev.marcal.chatvault.model.BucketFile
import org.springframework.core.io.Resource

interface BucketService {
    fun save(bucketFile: BucketFile)
    fun loadFileAsResource(bucketFile: BucketFile): Resource
    fun zipPendingImports(chatName: String? = null): Sequence<Resource>
    fun deleteZipImported(filename: String)
    fun saveToImportDir(bucketFile: BucketFile)
    fun saveTextToBucket(bucketFile: BucketFile, messages: Sequence<String>)
    fun loadBucketAsZip(path: String): Resource
}