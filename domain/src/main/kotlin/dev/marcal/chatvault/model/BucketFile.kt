package dev.marcal.chatvault.model

import java.io.File

class BucketFile(
    val bytes: ByteArray,
    val fileName: String,
    val address: Bucket,
) {


    fun file(root: String = "/"): File {
        val file = File(root + address.path + fileName)

        if (!file.toPath().normalize().startsWith(File(root).toPath())) {
            throw IllegalStateException("bad file path!out of root directory")
        }

        if (!file.toPath().normalize().startsWith(File(root + address.path).toPath())) {
            throw IllegalStateException("bad file path!out of bucket chat directory")
        }

        return file
    }
}
