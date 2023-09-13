package dev.marcal.chatvault.app_service.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.http.codec.ClientCodecConfigurer
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import java.time.Duration


@Configuration
class WebClientConfig {



    @Bean
    fun webClient(builder: WebClient.Builder): WebClient {

        val strategies = ExchangeStrategies.builder()
            .codecs(this::configureCodecs)
            .build()

        return builder
            .exchangeStrategies(strategies)
            .clientConnector(ReactorClientHttpConnector(reactor.netty.http.client.HttpClient.create().responseTimeout(Duration.ofSeconds(30))))
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build()
    }

    private fun configureCodecs(configurer: ClientCodecConfigurer) {
        configurer.defaultCodecs()
            .maxInMemorySize(500 * 1024 * 1024)
    }
}