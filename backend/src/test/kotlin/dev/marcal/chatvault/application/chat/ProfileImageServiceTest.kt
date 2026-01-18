package dev.marcal.chatvault.application.chat

import dev.marcal.chatvault.api.web.exception.AttachmentFinderException
import dev.marcal.chatvault.api.web.exception.ChatNotFoundException
import dev.marcal.chatvault.domain.bucket.BucketService
import dev.marcal.chatvault.domain.model.BucketFile
import dev.marcal.chatvault.domain.repository.ChatRepository
import dev.marcal.chatvault.shared.chatBucketInfoWith
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.context.ApplicationContext
import org.springframework.core.io.Resource
import java.io.ByteArrayInputStream
import kotlin.test.assertEquals

class ProfileImageServiceTest {
    private val bucketService: BucketService = mockk()
    private val chatRepository: ChatRepository = mockk()
    private val applicationContext: ApplicationContext = mockk()
    private val profileImageService = ProfileImageService(bucketService, chatRepository, applicationContext)

    @Test
    fun `should successfully update chat profile image`() {
        // Arrange
        val chatId = 1L
        val imageData = "fake image data".toByteArray()
        val inputStream = ByteArrayInputStream(imageData)
        val chatBucketInfo = chatBucketInfoWith(chatId = chatId)

        every { chatRepository.findChatBucketInfoByChatId(chatId) } returns chatBucketInfo
        every { bucketService.save(any()) } just Runs

        // Act
        profileImageService.updateImage(inputStream, chatId)

        // Assert
        verify { chatRepository.findChatBucketInfoByChatId(chatId) }
        verify { bucketService.save(any<BucketFile>()) }
    }

    @Test
    fun `should throw ChatNotFoundException when updating image for non-existent chat`() {
        // Arrange
        val nonExistentChatId = 999L
        val inputStream = ByteArrayInputStream("image".toByteArray())

        every { chatRepository.findChatBucketInfoByChatId(nonExistentChatId) } returns null

        // Act & Assert
        val exception =
            assertThrows<ChatNotFoundException> {
                profileImageService.updateImage(inputStream, nonExistentChatId)
            }
        assertEquals(
            "Unable to update chat image because chat $nonExistentChatId was not found",
            exception.message,
        )
    }

    @Test
    fun `should successfully retrieve existing profile image`() {
        // Arrange
        val chatId = 2L
        val chatBucketInfo = chatBucketInfoWith(chatId = chatId)
        val mockResource = mockk<Resource>()

        every { chatRepository.findChatBucketInfoByChatId(chatId) } returns chatBucketInfo
        every { bucketService.loadFileAsResource(any()) } returns mockResource

        // Act
        val result = profileImageService.getImage(chatId)

        // Assert
        assertEquals(mockResource, result)
        verify { bucketService.loadFileAsResource(any()) }
    }

    @Test
    fun `should return default avatar when profile image not found`() {
        // Arrange
        val chatId = 3L
        val chatBucketInfo = chatBucketInfoWith(chatId = chatId)
        val defaultAvatarResource = mockk<Resource>()

        every { chatRepository.findChatBucketInfoByChatId(chatId) } returns chatBucketInfo
        every { bucketService.loadFileAsResource(any()) } throws AttachmentFinderException("File not found", null)
        every { applicationContext.getResource("classpath:public/default-avatar.png") } returns defaultAvatarResource

        // Act
        val result = profileImageService.getImage(chatId)

        // Assert
        assertEquals(defaultAvatarResource, result)
        verify { applicationContext.getResource("classpath:public/default-avatar.png") }
    }

    @Test
    fun `should throw ChatNotFoundException when getting image for non-existent chat`() {
        // Arrange
        val nonExistentChatId = 999L

        every { chatRepository.findChatBucketInfoByChatId(nonExistentChatId) } returns null

        // Act & Assert
        val exception =
            assertThrows<ChatNotFoundException> {
                profileImageService.getImage(nonExistentChatId)
            }
        assertEquals(
            "Unable to retrieve chat image because chat $nonExistentChatId was not found",
            exception.message,
        )
    }

    @Test
    fun `should use correct file name when saving profile image`() {
        // Arrange
        val chatId = 5L
        val chatBucketInfo = chatBucketInfoWith(chatId = chatId)
        val imageData = ByteArray(1024)
        val inputStream = ByteArrayInputStream(imageData)

        every { chatRepository.findChatBucketInfoByChatId(chatId) } returns chatBucketInfo
        every { bucketService.save(any()) } just Runs

        // Act
        profileImageService.updateImage(inputStream, chatId)

        // Assert - Verify that the correct file name is used
        verify {
            bucketService.save(
                match<BucketFile> { bucketFile ->
                    bucketFile.fileName == "profile-image.jpg"
                },
            )
        }
    }

    @Test
    fun `should handle large image files`() {
        // Arrange
        val chatId = 6L
        val chatBucketInfo = chatBucketInfoWith(chatId = chatId)
        val largeImageData = ByteArray(10 * 1024 * 1024) // 10MB
        val inputStream = ByteArrayInputStream(largeImageData)

        every { chatRepository.findChatBucketInfoByChatId(chatId) } returns chatBucketInfo
        every { bucketService.save(any()) } just Runs

        // Act
        profileImageService.updateImage(inputStream, chatId)

        // Assert
        verify { bucketService.save(any()) }
    }
}
