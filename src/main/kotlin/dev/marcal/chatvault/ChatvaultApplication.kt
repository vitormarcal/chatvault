package dev.marcal.chatvault

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ChatvaultApplication

fun main(args: Array<String>) {
    runApplication<ChatvaultApplication>(*args)
}
