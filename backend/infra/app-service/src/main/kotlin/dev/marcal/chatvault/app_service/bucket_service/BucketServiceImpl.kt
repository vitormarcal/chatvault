package dev.marcal.chatvault.app_service.bucket_service

import dev.marcal.chatvault.in_out_boundary.output.exceptions.AttachmentFinderException
import dev.marcal.chatvault.in_out_boundary.output.exceptions.AttachmentNotFoundException
import dev.marcal.chatvault.model.Bucket
import dev.marcal.chatvault.model.BucketFile
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.PropertySource
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.stereotype.Service
import java.io.File
import java.io.FileFilter
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException


@Service
@PropertySource("classpath:appservice.properties")
class BucketServiceImpl(
    @Value("\${app.bucket.root}") val bucketRootPath: String,
    @Value("\${app.bucket.import}") val bucketImportPath: String
) : BucketService {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    @PostConstruct
    fun init() {
        createBucketIfNotExists(bucketRootPath)
        createBucketIfNotExists(bucketImportPath)
    }


    override fun save(bucketFile: BucketFile) {
        try {
            val file = bucketFile.file(bucketRootPath).also { createBucketIfNotExists(it.parentFile) }

            FileOutputStream(file).use { fos ->
                fos.write(bucketFile.bytes)
                fos.flush()
            }

            logger.info("File save at ${file.absolutePath}")
        } catch (e: FileNotFoundException) {
            throw BucketServiceException("file not found when try to save ${bucketFile.fileName}", e)
        } catch (e: IOException) {
            throw BucketServiceException("I/O error when try to save ${bucketFile.fileName}", e)
        } catch (e: Exception) {
            throw BucketServiceException("not mapped error when try to save ${bucketFile.fileName}", e)
        }
    }

    override fun zipPendingImports(): Sequence<Resource> {
        try {
            return File(bucketImportPath)
                .getDirectoriesWithContentAndZipFiles()
                .asSequence()
                .map { chatGroupDir ->
                    if (chatGroupDir.name.endsWith(".zip")) {
                        UrlResource(chatGroupDir.toURI())
                    } else {
                        DirectoryZipper.zip(chatGroupDir)
                    }

                }
        } catch (e: Exception) {
            throw BucketServiceException(message = "Fail to zip pending imports", throwable = e)
        }

    }

    override fun deleteZipImported(filename: String) {
        val toDelete = BucketFile(
            fileName = filename,
            address = Bucket(path = "/"),
            bytes = ByteArray(0)
        ).file(root = bucketImportPath)
        toDelete.delete()
    }

    override fun loadFileAsResource(bucketFile: BucketFile): Resource {
        try {
            val file = bucketFile.file(bucketRootPath)
            val resource = UrlResource(file.toURI())

            return resource.takeIf { it.exists() } ?: throw AttachmentNotFoundException("file not found ${file.name}")
        } catch (e: Exception) {
            throw AttachmentFinderException("failed to load file ${bucketFile.fileName}", e)
        }
    }

    private fun createBucketIfNotExists(path: String) {
        createBucketIfNotExists(File(path))
    }

    private fun createBucketIfNotExists(file: File) {
        file.takeIf { !it.exists() }?.also {
            logger.info("bucket $it not exists, creating...")
            it.mkdirs()
            logger.info("bucket $it created")
        }
    }


}


fun File.getDirectoriesWithContentAndZipFiles(): Array<out File> {
    return this.listFilesName {
        it.isDirectory && (it.list() ?: emptyArray()).isNotEmpty() ||
                it.name.endsWith(".zip")
    }
}

fun File.listFilesName(filter: FileFilter? = null): Array<File> = this.listFiles(filter) ?: emptyArray()