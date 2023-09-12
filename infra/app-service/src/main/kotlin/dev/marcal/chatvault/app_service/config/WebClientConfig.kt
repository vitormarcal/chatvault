package dev.marcal.chatvault.app_service.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import java.time.Duration

@Configuration
class WebClientConfig {



    @Bean
    fun webClient(builder: WebClient.Builder): WebClient {
        return builder
            .clientConnector(ReactorClientHttpConnector(reactor.netty.http.client.HttpClient.create().responseTimeout(Duration.ofSeconds(30))))
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build()
    }
}