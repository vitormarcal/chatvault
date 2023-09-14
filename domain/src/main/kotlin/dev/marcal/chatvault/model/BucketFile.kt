package dev.marcal.chatvault.model

import java.io.File

class BucketFile(
    val bytes: ByteArray,
    val fileName: String,
    val address: Bucket,
) {


    fun file(root: String = "/"): File {
        return File(root + address.path + fileName)
    }
}
