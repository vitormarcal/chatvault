package dev.marcal.chatvault.app_service.bucket_service

import dev.marcal.chatvault.in_out_boundary.output.exceptions.AttachmentFinderException
import dev.marcal.chatvault.in_out_boundary.output.exceptions.AttachmentNotFoundException
import dev.marcal.chatvault.in_out_boundary.output.exceptions.BucketServiceException
import dev.marcal.chatvault.model.Bucket
import dev.marcal.chatvault.model.BucketFile
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.PropertySource
import org.springframework.core.io.InputStreamResource
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.stereotype.Service
import java.io.*
import java.nio.file.Files
import java.nio.file.StandardCopyOption


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

    fun saveToBucket(bucketFile: BucketFile, bucketRootPath: String) {
        try {
            val file = bucketFile.file(bucketRootPath).also { createBucketIfNotExists(it.parentFile) }

            val bytes = bucketFile.bytes
            if (bytes != null) {
                FileOutputStream(file).use { fos ->
                    fos.write(bytes)
                    fos.flush()
                }
            } else {
                val inputStream = requireNotNull(bucketFile.stream)
                Files.copy(inputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING)
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

    override fun save(bucketFile: BucketFile) {
        saveToBucket(bucketFile, bucketRootPath)
    }

    override fun saveToImportDir(bucketFile: BucketFile) {
        saveToBucket(bucketFile, bucketImportPath)
    }

    override fun saveTextToBucket(bucketFile: BucketFile, messages: Sequence<String>) {
        val file = bucketFile.file(bucketRootPath)
        BufferedWriter(FileWriter(file)).use { writer ->
            messages.forEach { messageLine ->
                writer.write(messageLine)
                writer.newLine()
            }
        }
    }

    override fun loadBucketAsZip(path: String): Resource {
        try {
            return File(bucketRootPath).getDirectoriesWithContentAndZipFiles()
                .first { path == it.name }
                .let { dir ->
                    DirectoryZipper.zip(dir).let { resource ->
                        InputStreamResource(object : FileInputStream(resource.file) {
                            @Throws(IOException::class)
                            override fun close() {
                                super.close()
                                val isDeleted: Boolean = resource.file.delete()
                                logger.info(
                                    "export:'{}':" + if (isDeleted) "deleted" else "preserved", resource.file.name
                                )
                            }
                        })
                    }
                }
        } catch (e: Exception) {
            throw BucketServiceException(message = "Fail to zip bucket", throwable = e)
        }

    }

    override fun zipPendingImports(chatName: String?): Sequence<Resource> {
        try {
            return File(bucketImportPath)
                .getDirectoriesWithContentAndZipFiles()
                .asSequence()
                .filter { chatName == null || chatName == it.name }
                .map { chatGroupDir ->
                    if (chatGroupDir.name.endsWith(".zip")) {
                        UrlResource(chatGroupDir.toURI())
                    } else {
                        DirectoryZipper.zipAndDeleteSource(chatGroupDir)
                    }

                }
        } catch (e: Exception) {
            throw BucketServiceException(message = "Fail to zip pending imports", throwable = e)
        }

    }

    override fun deleteZipImported(filename: String) {
        val toDelete = BucketFile(
            fileName = filename,
            address = Bucket(path = "/")
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