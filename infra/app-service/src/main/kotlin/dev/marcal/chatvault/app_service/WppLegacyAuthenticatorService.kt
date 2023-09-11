package dev.marcal.chatvault.app_service

import dev.marcal.chatvault.app_service.dto.AuthToken
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.annotation.PropertySource
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Service
@PropertySource("classpath:appservice.properties")
class WppLegacyAuthenticatorService(
    private val webClient: WebClient,
    @Value("\${app.legacy.login}") private val loginUrl: String,
    @Value("\${app.legacy.email}") private val email: String,
    @Value("\${app.legacy.psw}") private val psw: String,
) {


    @Cacheable("wpp-legacy-token")
    fun getToken(): Mono<String> {
        return webClient
            .post()
            .uri(loginUrl)
            .bodyValue(mapOf("email" to email, "password" to psw))
            .retrieve()
            .bodyToMono(AuthToken::class.java)
            .map { it.token }
    }
}