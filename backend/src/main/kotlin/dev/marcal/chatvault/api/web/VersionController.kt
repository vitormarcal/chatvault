package dev.marcal.chatvault.api.web

import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class VersionController(
    @Value("\${chatvault.version}") private val appVersion: String,
) {
    @GetMapping("/api/version")
    fun getVersion() = mapOf("version" to appVersion)
}
