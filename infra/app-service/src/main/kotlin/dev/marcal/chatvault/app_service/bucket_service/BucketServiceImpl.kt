package dev.marcal.chatvault.app_service.bucket_service

import dev.marcal.chatvault.model.BucketFile
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.PropertySource
import org.springframework.stereotype.Service
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

@Service
@PropertySource("classpath:appservice.properties")
class BucketServiceImpl(
    @Value("\${app.bucket.root}") val bucketRootPath: String
) : BucketService {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    @PostConstruct
    fun init() {
        createBucketIfNotExists(bucketRootPath)
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