package dev.marcal.chatvault.service

import dev.marcal.chatvault.in_out_boundary.input.PendingChatFile
import java.io.InputStream

interface BucketDiskImporter {

    fun execute()

    fun saveToImportDir(pendingList: List<PendingChatFile>)
}