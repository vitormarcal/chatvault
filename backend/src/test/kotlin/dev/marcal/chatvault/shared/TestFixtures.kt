package dev.marcal.chatvault.shared

import dev.marcal.chatvault.api.dto.input.NewAttachmentInput
import dev.marcal.chatvault.api.dto.input.NewMessageInput
import dev.marcal.chatvault.api.dto.output.AttachmentInfoOutput
import dev.marcal.chatvault.api.dto.output.ChatLastMessageOutput
import dev.marcal.chatvault.api.dto.output.MessageOutput
import dev.marcal.chatvault.domain.bucket.BucketService
import dev.marcal.chatvault.domain.model.Attachment
import dev.marcal.chatvault.domain.model.AttachmentSummary
import dev.marcal.chatvault.domain.model.Author
import dev.marcal.chatvault.domain.model.AuthorType
import dev.marcal.chatvault.domain.model.Bucket
import dev.marcal.chatvault.domain.model.Chat
import dev.marcal.chatvault.domain.model.ChatBucketInfo
import dev.marcal.chatvault.domain.model.ChatLastMessage
import dev.marcal.chatvault.domain.model.Content
import dev.marcal.chatvault.domain.model.Message
import dev.marcal.chatvault.domain.model.Page
import dev.marcal.chatvault.domain.repository.ChatRepository
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import org.springframework.core.io.Resource
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.time.LocalDateTime

/**
 * Shared test fixtures for all test classes.
 * Provides factory functions for creating domain objects and DTOs with sensible defaults.
 */

// ============================================================================
// DOMAIN MODEL FACTORIES
// ============================================================================

/**
 * Create a Chat domain object with default values
 */
fun chatWith(
    id: Long = 1L,
    externalId: String? = "external-1",
    name: String = "Test Chat",
    messages: List<Message> = emptyList(),
    bucket: Bucket = bucketWith(),
): Chat = Chat(
    id = id,
    externalId = externalId,
    name = name,
    messages = messages,
    bucket = bucket,
)

/**
 * Create a Bucket domain object with default values
 */
fun bucketWith(
    path: String = "/chat-1",
): Bucket = Bucket(path = path)

/**
 * Create a ChatBucketInfo domain object with default values
 */
fun chatBucketInfoWith(
    chatId: Long = 1L,
    bucket: Bucket = bucketWith(path = "/chat-$chatId"),
): ChatBucketInfo = ChatBucketInfo(
    chatId = chatId,
    bucket = bucket,
)

/**
 * Create a Message domain object with default values
 */
fun messageWith(
    id: Long? = 1L,
    author: String = "Test User",
    authorType: AuthorType = AuthorType.USER,
    content: String = "Test message content",
    attachment: Attachment? = null,
    createdAt: LocalDateTime = LocalDateTime.of(2023, 1, 1, 12, 0),
    externalId: String? = null,
): Message = Message(
    id = id,
    author = Author(name = author, type = authorType),
    content = Content(text = content, attachment = attachment),
    createdAt = createdAt,
    externalId = externalId,
)

/**
 * Create an Author domain object with default values
 */
fun authorWith(
    name: String = "Test User",
    type: AuthorType = AuthorType.USER,
): Author = Author(name = name, type = type)

/**
 * Create Content domain object with default values
 */
fun contentWith(
    text: String = "Test content",
    attachment: Attachment? = null,
): Content = Content(text = text, attachment = attachment)

/**
 * Create an Attachment domain object with default values
 */
fun attachmentWith(
    name: String = "test.pdf",
    bucket: Bucket = bucketWith(),
): Attachment = Attachment(name = name, bucket = bucket)

/**
 * Create an AttachmentSummary domain object with default values
 */
fun attachmentSummaryWith(
    id: Long = 1L,
    name: String = "test.pdf",
): AttachmentSummary = AttachmentSummary(id = id, name = name)

/**
 * Create a Page<T> object for pagination tests (domain model)
 */
fun <T> pageWith(
    data: List<T> = emptyList(),
    page: Int = 0,
    items: Int = 10,
    totalItems: Long = data.size.toLong(),
    totalPages: Int = if (totalItems == 0L) 0 else (totalItems.toInt() + items - 1) / items,
): Page<T> = Page(
    page = page,
    totalPages = totalPages,
    totalItems = totalItems,
    items = items,
    data = data,
)

/**
 * Create ChatLastMessage for conversa listing
 */
fun chatLastMessageWith(
    chatId: Long = 1L,
    chatName: String = "Test Chat",
    author: Author = authorWith(),
    content: String = "Last message",
    msgCreatedAt: LocalDateTime = LocalDateTime.of(2023, 1, 1, 12, 0),
    msgCount: Long = 10L,
): ChatLastMessage = ChatLastMessage(
    chatId = chatId,
    chatName = chatName,
    author = author,
    content = content,
    msgCreatedAt = msgCreatedAt,
    msgCount = msgCount,
)

// ============================================================================
// DTO FACTORIES (Input)
// ============================================================================

/**
 * Create a NewMessageInput DTO with default values
 */
fun newMessageInputWith(
    chatId: Long = 1L,
    authorName: String = "Test User",
    content: String = "Test message",
    createdAt: LocalDateTime? = LocalDateTime.of(2023, 1, 1, 12, 0),
    externalId: String? = null,
    attachment: NewAttachmentInput? = null,
): NewMessageInput = NewMessageInput(
    chatId = chatId,
    authorName = authorName,
    content = content,
    createdAt = createdAt,
    externalId = externalId,
    attachment = attachment,
)

/**
 * Create a NewAttachmentInput DTO with default values
 */
fun newAttachmentInputWith(
    name: String = "test.pdf",
    content: String = "file content",
): NewAttachmentInput = NewAttachmentInput(
    name = name,
    content = content,
)

// ============================================================================
// DTO FACTORIES (Output)
// ============================================================================

/**
 * Create a MessageOutput DTO with default values
 */
fun messageOutputWith(
    id: Long? = 1L,
    author: String? = "Test User",
    authorType: String = "USER",
    content: String = "Test message content",
    attachmentName: String? = null,
    createdAt: LocalDateTime = LocalDateTime.of(2023, 1, 1, 12, 0),
): MessageOutput = MessageOutput(
    id = id,
    author = author,
    authorType = authorType,
    content = content,
    attachmentName = attachmentName,
    createdAt = createdAt,
)


/**
 * Create a ChatLastMessageOutput DTO with default values
 */
fun chatLastMessageOutputWith(
    chatId: Long = 1L,
    chatName: String = "Test Chat",
    authorName: String = "User",
    authorType: String = "USER",
    content: String = "Last message",
    msgCreatedAt: LocalDateTime = LocalDateTime.of(2023, 1, 1, 12, 0),
    msgCount: Long = 10L,
): ChatLastMessageOutput = ChatLastMessageOutput(
    chatId = chatId,
    chatName = chatName,
    authorName = authorName,
    authorType = authorType,
    content = content,
    msgCreatedAt = msgCreatedAt,
    msgCount = msgCount,
)

/**
 * Create an AttachmentInfoOutput DTO with default values
 */
fun attachmentInfoOutputWith(
    id: Long = 1L,
    name: String = "test.pdf",
): AttachmentInfoOutput = AttachmentInfoOutput(
    id = id,
    name = name,
)

// ============================================================================
// MOCK FACTORIES
// ============================================================================

/**
 * Create a mocked ChatRepository with sensible defaults
 */
fun mockChatRepository(
    chatBucketInfo: ChatBucketInfo? = chatBucketInfoWith(),
    lastMessage: ChatLastMessage? = chatLastMessageWith(),
    allChats: Sequence<ChatLastMessage> = sequenceOf(lastMessage).filterNotNull(),
    messageCount: Long = 5L,
): ChatRepository = mockk {
    every { findChatBucketInfoByChatId(any()) } returns chatBucketInfo
    every { findLastMessageByChatId(any()) } returns lastMessage?.let {
        messageWith(
            author = it.author.name,
            content = it.content,
            createdAt = it.msgCreatedAt,
        )
    }
    every { findAllChatsWithLastMessage() } returns allChats
    every { countChatMessages(any()) } returns messageCount
    every { saveNewMessages(any()) } just Runs
    every { deleteChat(any()) } just Runs
    every { setChatNameByChatId(any(), any()) } just Runs
    every { existsByChatId(any()) } returns (chatBucketInfo != null)
    every { existsByExternalId(any()) } returns false
}

/**
 * Create a mocked BucketService with sensible defaults
 */
fun mockBucketService(): BucketService = mockk {
    every { save(any()) } just Runs
    every { delete(any()) } just Runs
    every { loadFileAsResource(any()) } returns mockk()
    every { loadBucketAsZip(any()) } returns mockk()
    every { saveToImportDir(any()) } just Runs
    every { saveTextToBucket(any(), any()) } just Runs
    every { zipPendingImports(any()) } returns emptySequence()
    every { deleteZipImported(any()) } just Runs
    every { loadBucketListAsZip() } returns mockk()
}

/**
 * Create lists of test messages for bulk operations
 */
fun messageListWith(
    count: Int = 5,
    author: String = "Test User",
    startId: Long = 1L,
): List<Message> = (0 until count).map { index ->
    messageWith(
        id = startId + index,
        author = author,
        content = "Message $index",
        createdAt = LocalDateTime.of(2023, 1, 1, 12, index),
        externalId = "external-$index",
    )
}

/**
 * Create lists of test message outputs for API responses
 */
fun messageOutputListWith(
    count: Int = 5,
    author: String = "Test User",
    startId: Long = 1L,
): List<MessageOutput> = (0 until count).map { index ->
    messageOutputWith(
        id = startId + index,
        author = author,
        content = "Message $index",
        createdAt = LocalDateTime.of(2023, 1, 1, 12, index),
    )
}

/**
 * Create lists of test chats for bulk operations
 */
fun chatListWith(
    count: Int = 5,
    startId: Long = 1L,
): List<Chat> = (0 until count).map { index ->
    val chatId = startId + index
    chatWith(
        id = chatId,
        externalId = "external-$index",
        name = "Chat $index",
        bucket = bucketWith(path = "/chat-$chatId"),
    )
}
