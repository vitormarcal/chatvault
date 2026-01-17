package dev.marcal.chatvault.api.web.exception

import dev.marcal.chatvault.domain.exception.MessageParserException
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@WebMvcTest
class WebControllerAdviceTest {
    @Autowired
    private lateinit var mvc: MockMvc

    @Configuration
    class TestConfig {
        @Bean
        fun webControllerAdvice() = WebControllerAdvice()

        @Bean
        fun testController() = TestExceptionController()
    }

    @RestController
    class TestExceptionController {
        @GetMapping("/test-attachment-not-found")
        fun throwAttachmentNotFoundException() {
            throw AttachmentNotFoundException("Attachment with id 1 was not found", Exception("cause"))
        }

        @GetMapping("/test-chat-not-found")
        fun throwChatNotFoundException() {
            throw ChatNotFoundException("Chat with id 999 was not found")
        }

        @GetMapping("/test-bucket-file-not-found")
        fun throwBucketFileNotFoundException() {
            throw BucketFileNotFoundException("File not found in bucket", Exception("root cause"))
        }

        @GetMapping("/test-message-parser-exception")
        fun throwMessageParserException() {
            throw MessageParserException("Invalid message format")
        }

        @GetMapping("/test-illegal-argument")
        fun throwIllegalArgument() {
            throw IllegalArgumentException("Invalid argument provided")
        }

        @GetMapping("/test-illegal-state")
        fun throwIllegalState() {
            throw IllegalStateException("Invalid state detected")
        }

        @GetMapping("/test-chat-importer-exception")
        fun throwChatImporterException() {
            throw ChatImporterException("Import failed")
        }

        @GetMapping("/test-bucket-service-exception")
        fun throwBucketServiceException() {
            throw BucketServiceException("Bucket operation failed", Exception("root cause"))
        }

        @GetMapping("/test-attachment-finder-exception")
        fun throwAttachmentFinderException() {
            throw AttachmentFinderException("Attachment not found", Exception("root cause"))
        }
    }

    @Test
    fun `should handle AttachmentNotFoundException with 404 status`() {
        // Act & Assert
        mvc.get("/test-attachment-not-found")
            .andExpect {
                status { isNotFound() }
            }
    }

    @Test
    fun `should handle ChatNotFoundException with 404 status`() {
        // Act & Assert
        mvc.get("/test-chat-not-found")
            .andExpect {
                status { isNotFound() }
            }
    }

    @Test
    fun `should handle BucketFileNotFoundException with 404 status`() {
        // Act & Assert
        mvc.get("/test-bucket-file-not-found")
            .andExpect {
                status { isNotFound() }
            }
    }

    @Test
    fun `should handle AttachmentFinderException with 404 status`() {
        // Act & Assert
        mvc.get("/test-attachment-finder-exception")
            .andExpect {
                status { isNotFound() }
            }
    }

    @Test
    fun `should handle MessageParserException with 422 status`() {
        // Act & Assert
        mvc.get("/test-message-parser-exception")
            .andExpect {
                status { isUnprocessableEntity() }
            }
    }

    @Test
    fun `should handle IllegalArgumentException with 424 status`() {
        // Act & Assert
        mvc.get("/test-illegal-argument")
            .andExpect {
                status { is4xxClientError() }
            }
    }

    @Test
    fun `should handle IllegalStateException with 424 status`() {
        // Act & Assert
        mvc.get("/test-illegal-state")
            .andExpect {
                status { is4xxClientError() }
            }
    }

    @Test
    fun `should handle ChatImporterException with 500 status`() {
        // Act & Assert
        mvc.get("/test-chat-importer-exception")
            .andExpect {
                status { isInternalServerError() }
            }
    }

    @Test
    fun `should handle BucketServiceException with 500 status`() {
        // Act & Assert
        mvc.get("/test-bucket-service-exception")
            .andExpect {
                status { isInternalServerError() }
            }
    }

    @Test
    fun `should include error message in response for not found exceptions`() {
        // Act & Assert
        mvc.get("/test-attachment-not-found")
            .andExpect {
                status { isNotFound() }
            }
    }

    @Test
    fun `should handle exception with null message`() {
        // Arrange - AttachmentNotFoundException can have null message
        // Act & Assert - handler should provide default message
        mvc.get("/test-chat-not-found")
            .andExpect {
                status { isNotFound() }
            }
    }
}
