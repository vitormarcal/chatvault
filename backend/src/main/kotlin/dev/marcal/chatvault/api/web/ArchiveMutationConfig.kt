package dev.marcal.chatvault.api.web

import dev.marcal.chatvault.application.archive.ArchiveGate
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.HandlerMapping
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class ArchiveMutationConfig : WebMvcConfigurer {
    @Bean
    fun archiveGate() = ArchiveGate()

    override fun addInterceptors(registry: InterceptorRegistry) {
        val gate = archiveGate()
        registry
            .addInterceptor(
                object : HandlerInterceptor {
                    override fun preHandle(
                        request: HttpServletRequest,
                        response: HttpServletResponse,
                        handler: Any,
                    ): Boolean {
                        val path =
                            request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE)?.toString()
                                ?: request.requestURI.removePrefix(request.contextPath)
                        val writes =
                            request.method in setOf("POST", "PUT", "PATCH", "DELETE") ||
                                (
                                    request.method == "GET" &&
                                        path.startsWith(
                                            "/api/chats/",
                                        ) &&
                                        (path.endsWith("/export") || path.endsWith("/export/all"))
                                )
                        if (writes && !path.startsWith("/api/archive-exports")) {
                            gate.beginMutation()
                            request.setAttribute("archiveMutation", true)
                        }
                        return true
                    }

                    override fun afterCompletion(
                        request: HttpServletRequest,
                        response: HttpServletResponse,
                        handler: Any,
                        ex: Exception?,
                    ) {
                        if (request.getAttribute("archiveMutation") == true) gate.endMutation()
                    }
                },
            ).addPathPatterns("/api/**")
    }
}
