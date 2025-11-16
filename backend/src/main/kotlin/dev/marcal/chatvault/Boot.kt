package dev.marcal.chatvault

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class Boot {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runApplication<Boot>(*args)
        }
    }
}
