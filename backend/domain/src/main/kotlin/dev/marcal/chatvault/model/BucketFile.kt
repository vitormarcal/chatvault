package dev.marcal.chatvault.model

import java.io.File
import java.nio.file.Paths

class BucketFile(
    val bytes: ByteArray,
    val fileName: String,
    val address: Bucket,
) {


    fun file(root: String = "/"): File {
        val path = Paths.get(root, address.path, fileName).normalize()

        if (!path.startsWith(File(root).toPath())) {
            throw IllegalStateException("bad file path!out of root directory")
        }

        if (!path.startsWith(Paths.get(root, address.path))) {
            throw IllegalStateException("bad file path!out of bucket chat directory")
        }

        return path.toFile()
    }
}
