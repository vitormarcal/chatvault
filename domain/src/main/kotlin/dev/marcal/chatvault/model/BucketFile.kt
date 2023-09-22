package dev.marcal.chatvault.model

import java.io.File
import java.lang.IllegalStateException

class BucketFile(
    val bytes: ByteArray,
    val fileName: String,
    val address: Bucket,
) {


    fun file(root: String = "/"): File {
        return File(root + address.path + fileName).apply {
            if (!this.toPath().normalize().startsWith(File(root).toPath())) {
                throw IllegalStateException("bad file path!out of root directory")
            }

            if (!this.toPath().normalize().startsWith(File(root + address.path).toPath())) {
                throw IllegalStateException("bad file path!out of bucket chat directory")
            }
        }
    }
}
