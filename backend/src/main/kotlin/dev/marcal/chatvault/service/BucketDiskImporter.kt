package dev.marcal.chatvault.service

import dev.marcal.chatvault.ioboundary.input.PendingChatFile

interface BucketDiskImporter {
    fun execute(chatName: String? = null)

    fun saveToImportDir(pendingList: List<PendingChatFile>)
}
