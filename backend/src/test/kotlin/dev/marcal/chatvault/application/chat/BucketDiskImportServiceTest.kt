package dev.marcal.chatvault.application.chat

import dev.marcal.chatvault.api.dto.input.FileTypeInputEnum
import dev.marcal.chatvault.api.dto.input.PendingChatFile
import dev.marcal.chatvault.domain.bucket.BucketService
import dev.marcal.chatvault.shared.chatBucketInfoWith
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.core.io.FileSystemResource
import java.io.ByteArrayInputStream
import kotlin.test.assertEquals

class BucketDiskImportServiceTest {
    private val bucketService: BucketService = mockk()
    private val chatImportService: ChatImportService = mockk()
    private val chatCreatorService: ChatCreatorService = mockk()
    private val bucketDiskImportService = BucketDiskImportService(
        bucketService = bucketService,
        chatFileImporter = chatImportService,
        chatCreator = chatCreatorService,
    )

    @Test
    fun `should save pending files to import directory`() {
        // Arrange
        val pendingFiles = listOf(
            PendingChatFile(
                fileName = "chat1.zip",
                chatName = "Chat 1",
                stream = ByteArrayInputStream("test1".toByteArray()),
            ),
            PendingChatFile(
                fileName = "chat2.zip",
                chatName = "Chat 2",
                stream = ByteArrayInputStream("test2".toByteArray()),
            ),
        )

        every { bucketService.saveToImportDir(any()) } returns Unit

        // Act
        bucketDiskImportService.saveToImportDir(pendingFiles)

        // Assert
        verify(exactly = 2) { bucketService.saveToImportDir(any()) }
    }

    @Test
    fun `should process pending imports and create chats`() {
        // Arrange
        val chatName = "Test Chat"
        val mockResource1 = mockk<org.springframework.core.io.Resource>()
        val mockResource2 = mockk<org.springframework.core.io.Resource>()

        every { bucketService.zipPendingImports(chatName) } returns sequenceOf(mockResource1, mockResource2)
        every { mockResource1.filename } returns "$chatName.zip"
        every { mockResource2.filename } returns "Another.zip"
        every { mockResource1.inputStream } returns ByteArrayInputStream("content1".toByteArray())
        every { mockResource2.inputStream } returns ByteArrayInputStream("content2".toByteArray())
        every { chatCreatorService.findOrCreateByName(any()) } returns chatBucketInfoWith(chatId = 1L)
        every { chatImportService.execute(chatId = any(), inputStream = any(), fileType = any()) } returns Unit
        every { bucketService.deleteZipImported(any()) } returns Unit

        // Act
        bucketDiskImportService.execute(chatName)

        // Assert
        verify(exactly = 2) { chatImportService.execute(chatId = any(), inputStream = any(), fileType = any()) }
        verify(exactly = 2) { bucketService.deleteZipImported(any()) }
    }

    @Test
    fun `should extract chat name from zip filename`() {
        // Arrange
        val zipName = "Important Chat"
        val mockResource = mockk<org.springframework.core.io.Resource>()

        every { bucketService.zipPendingImports(null) } returns sequenceOf(mockResource)
        every { mockResource.filename } returns "$zipName.zip"
        every { mockResource.inputStream } returns ByteArrayInputStream("test".toByteArray())
        every { chatCreatorService.findOrCreateByName(zipName) } returns chatBucketInfoWith(chatId = 2L)
        every { chatImportService.execute(chatId = any(), inputStream = any(), fileType = any()) } returns Unit
        every { bucketService.deleteZipImported(any()) } returns Unit

        // Act
        bucketDiskImportService.execute()

        // Assert
        verify { chatCreatorService.findOrCreateByName(zipName) }
    }

    @Test
    fun `should handle empty pending imports`() {
        // Arrange
        every { bucketService.zipPendingImports(any()) } returns emptySequence()

        // Act
        bucketDiskImportService.execute("NonExistent")

        // Assert
        verify(exactly = 0) { chatImportService.execute(chatId = any(), inputStream = any(), fileType = any()) }
        verify(exactly = 0) { bucketService.deleteZipImported(any()) }
    }

    @Test
    fun `should delete imported files after processing`() {
        // Arrange
        val mockResource = mockk<org.springframework.core.io.Resource>()
        val fileName = "processed.zip"

        every { bucketService.zipPendingImports(any()) } returns sequenceOf(mockResource)
        every { mockResource.filename } returns fileName
        every { mockResource.inputStream } returns ByteArrayInputStream("test".toByteArray())
        every { chatCreatorService.findOrCreateByName(any()) } returns chatBucketInfoWith(chatId = 3L)
        every { chatImportService.execute(chatId = any(), inputStream = any(), fileType = any()) } returns Unit
        every { bucketService.deleteZipImported(fileName) } returns Unit

        // Act
        bucketDiskImportService.execute()

        // Assert
        verify { bucketService.deleteZipImported(fileName) }
    }

    @Test
    fun `should save single pending file correctly`() {
        // Arrange
        val pendingFile = listOf(
            PendingChatFile(
                fileName = "single.zip",
                chatName = "Single Chat",
                stream = ByteArrayInputStream("content".toByteArray()),
            ),
        )

        every { bucketService.saveToImportDir(any()) } returns Unit

        // Act
        bucketDiskImportService.saveToImportDir(pendingFile)

        // Assert
        verify(exactly = 1) { bucketService.saveToImportDir(any()) }
    }
}
