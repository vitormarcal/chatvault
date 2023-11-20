package dev.marcal.chatvault.web.config

import dev.marcal.chatvault.in_out_boundary.output.exceptions.AttachmentFinderException
import dev.marcal.chatvault.in_out_boundary.output.exceptions.AttachmentNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException

@ControllerAdvice
class WebControllerAdvice : ResponseEntityExceptionHandler() {

    @ExceptionHandler(value = [AttachmentNotFoundException::class, AttachmentFinderException::class])
    fun handleNotFound(
        ex: RuntimeException, request: WebRequest
    ): ResponseStatusException {
        return ResponseStatusException(
            HttpStatus.NOT_FOUND, ex.message ?: "Resource was not found", ex
        )
    }

    @ExceptionHandler(value = [IllegalArgumentException::class, IllegalStateException::class])
    fun handleConflict(
        ex: RuntimeException, request: WebRequest
    ): ResponseStatusException {
        return ResponseStatusException(
            HttpStatus.FAILED_DEPENDENCY, ex.message ?: "Invalid state or requirements", ex
        )
    }

}