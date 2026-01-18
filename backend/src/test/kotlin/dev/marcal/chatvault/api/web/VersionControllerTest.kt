package dev.marcal.chatvault.api.web

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import kotlin.test.assertTrue

@WebMvcTest(VersionController::class)
@TestPropertySource(properties = ["chatvault.version=1.0.0"])
class VersionControllerTest {
    @Autowired
    private lateinit var mvc: MockMvc

    @Test
    fun `should return application version`() {
        // Act & Assert
        mvc
            .get("/api/version")
            .andExpect {
                status { isOk() }
                content { contentType("application/json") }
            }.andReturn()
            .response
            .contentAsString
            .let { content ->
                assertTrue(content.contains("\"version\""), "Response should contain version key")
                assertTrue(content.contains("1.0.0"), "Response should contain version value")
            }
    }

    @Test
    fun `should return version in correct JSON format`() {
        // Act & Assert
        mvc
            .get("/api/version")
            .andExpect {
                status { isOk() }
                jsonPath("$.version") { value("1.0.0") }
            }
    }
}
