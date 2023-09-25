package dev.marcal.chatvault.app_service.bucket_service

import org.springframework.core.io.UrlResource
import org.springframework.util.StreamUtils
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object DirectoryZipper {

    fun zip(directory: File): UrlResource {
        val zipFileName: String = directory.getName() + ".zip"
        val zipFile = File(directory.getParent(), zipFileName)

        try {
            FileOutputStream(zipFile).use { fos ->
                ZipOutputStream(fos).use { zipOut ->
                    directory.listFilesName().forEach { file ->
                        FileInputStream(file).use { fis ->
                            val zipEntry = ZipEntry(file.getName())
                            zipOut.putNextEntry(zipEntry)
                            StreamUtils.copy(fis, zipOut)
                            zipOut.closeEntry()
                        }

                    }
                }
            }
            directory.listFilesName().forEach { it.delete() }
            return UrlResource(zipFile.toURI())
        } catch (e: IOException) {
            throw RuntimeException("fail to zip file  $zipFileName")
        }
    }

}