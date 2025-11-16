package dev.marcal.chatvault.appservice.bucketservice

import org.springframework.core.io.UrlResource
import org.springframework.util.StreamUtils
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object DirectoryZipper {
    fun zip(
        directory: File,
        targetDir: String,
        saveInSameBaseDir: Boolean = true,
    ): UrlResource {
        val zipFileName: String = "${directory.name}.zip"
        val zipFile = File(targetDir, zipFileName)

        try {
            FileOutputStream(zipFile).use { fos ->
                ZipOutputStream(fos).use { zipOut ->
                    zipDirectory(directory, directory.name, zipOut, saveInSameBaseDir)
                }
            }
            return UrlResource(zipFile.toURI())
        } catch (e: IOException) {
            throw RuntimeException("Failed to zip file $zipFileName", e)
        }
    }

    private fun zipDirectory(
        sourceDir: File,
        baseDirName: String,
        zipOut: ZipOutputStream,
        saveInSameBaseDir: Boolean = true,
    ) {
        val files = sourceDir.listFiles() ?: return

        for (file in files) {
            val baseDir = if (saveInSameBaseDir) file.name else "$baseDirName/${file.name}"
            if (file.isDirectory) {
                zipDirectory(file, baseDir, zipOut, saveInSameBaseDir)
            } else {
                FileInputStream(file).use { fis ->
                    val zipEntry = ZipEntry(baseDir)
                    zipOut.putNextEntry(zipEntry)
                    StreamUtils.copy(fis, zipOut)
                    zipOut.closeEntry()
                }
            }
        }
    }

    fun zipAndDeleteSource(directory: File): UrlResource =
        zip(directory, directory.parent, saveInSameBaseDir = true).also { _ ->
            directory.listFiles()?.forEach { it.deleteRecursively() }
        }
}
