package dev.marcal.chatvault.appservice.bucketservice

import dev.marcal.chatvault.domain.bucketservice.BucketService
import dev.marcal.chatvault.domain.model.Bucket
import dev.marcal.chatvault.domain.model.BucketFile
import dev.marcal.chatvault.ioboundary.output.exceptions.AttachmentFinderException
import dev.marcal.chatvault.ioboundary.output.exceptions.AttachmentNotFoundException
import dev.marcal.chatvault.ioboundary.output.exceptions.BucketFileNotFoundException
import dev.marcal.chatvault.ioboundary.output.exceptions.BucketServiceException
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.InputStreamResource
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.stereotype.Service
import java.io.BufferedWriter
import java.io.File
import java.io.FileFilter
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.FileWriter
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardCopyOption

@Service
class BucketServiceImpl(
    @Value("\${chatvault.bucket.root}") val bucketRootPath: String,
    @Value("\${chatvault.bucket.import}") val bucketImportPath: String,
    @Value("\${chatvault.bucket.export}") val bucketExportPath: String,
) : BucketService {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    @PostConstruct
    fun init() {
        createBucketIfNotExists(bucketRootPath)
        createBucketIfNotExists(bucketImportPath)
        createBucketIfNotExists(bucketExportPath)
    }

    fun saveToBucket(
        bucketFile: BucketFile,
        bucketRootPath: String,
    ) {
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

    override fun saveTextToBucket(
        bucketFile: BucketFile,
        messages: Sequence<String>,
    ) {
        try {
            val file = bucketFile.file(bucketRootPath)
            BufferedWriter(FileWriter(file)).use { writer ->
                messages.forEach { messageLine ->
                    writer.write(messageLine)
                    writer.newLine()
                }
            }
        } catch (ex: FileNotFoundException) {
            throw BucketFileNotFoundException("File to save ${bucketFile.fileName}. Bucket chat was not found", ex)
        } catch (ex: Exception) {
            throw BucketServiceException("File to save ${bucketFile.fileName}. Unexpected error", ex)
        }
    }

    override fun loadBucketAsZip(path: String): Resource {
        try {
            return File(bucketRootPath)
                .getDirectoriesWithContentAndZipFiles()
                .first { path == it.name }
                .let { dir -> zip(dir, targetDir = bucketExportPath) }
        } catch (e: Exception) {
            throw BucketServiceException(message = "Fail to zip bucket", throwable = e)
        }
    }

    override fun loadBucketListAsZip(): Resource = zip(File(bucketRootPath), targetDir = bucketExportPath)

    private fun zip(
        dir: File,
        targetDir: String,
    ) = DirectoryZipper.zip(dir, targetDir, saveInSameBaseDir = false).let { resource ->
        InputStreamResource(
            object : FileInputStream(resource.file) {
                @Throws(IOException::class)
                override fun close() {
                    super.close()
                    val isDeleted: Boolean = resource.file.delete()
                    logger.info(
                        "export:'{}':" + if (isDeleted) "deleted" else "preserved",
                        resource.file.name,
                    )
                }
            },
        )
    }

    override fun delete(bucketFile: BucketFile) {
        val file = bucketFile.file(bucketRootPath)
        logger.info("start to delete bucket at: $file")

        if (!file.exists()) {
            return
        }

        Files
            .walk(file.toPath())
            .sorted(reverseOrder())
            .forEach {
                logger.info("item to delete: {}", it)
                Files.delete(it)
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
        val toDelete =
            BucketFile(
                fileName = filename,
                address = Bucket(path = "/"),
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
            if (it.mkdirs()) {
                logger.info("bucket $it created")
            } else {
                throw BucketServiceException(
                    message =
                        "check if the user has permission to write to chatvault directories: $file." +
                            " You can change the app.bucket.root environment variable" +
                            " for the location of ChatVault files ",
                    null,
                )
            }
        }
    }
}

fun File.getDirectoriesWithContentAndZipFiles(): Array<out File> =
    this.listFilesName {
        it.isDirectory &&
            (it.list() ?: emptyArray()).isNotEmpty() ||
            it.name.endsWith(".zip")
    }

fun File.listFilesName(filter: FileFilter? = null): Array<File> = this.listFiles(filter) ?: emptyArray()
