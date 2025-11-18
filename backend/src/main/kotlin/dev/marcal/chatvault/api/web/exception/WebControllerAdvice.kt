package dev.marcal.chatvault.api.web.exception

import dev.marcal.chatvault.domain.exception.MessageParserException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@ControllerAdvice
class WebControllerAdvice : ResponseEntityExceptionHandler() {
    private val log = LoggerFactory.getLogger(this.javaClass)

    @ExceptionHandler(
        value = [
            AttachmentNotFoundException::class,
            AttachmentFinderException::class,
            ChatNotFoundException::class,
            BucketFileNotFoundException::class,
        ],
    )
    fun handleNotFound(
        ex: RuntimeException,
        request: WebRequest,
    ): ResponseStatusException =
        ResponseStatusException(
            HttpStatus.NOT_FOUND,
            ex.message ?: "Resource was not found",
            ex,
        )

    @ExceptionHandler(value = [MessageParserException::class])
    fun handleUnprocessableEntity(
        ex: RuntimeException,
        request: WebRequest,
    ): ResponseStatusException {
        log.error("The request failed due to an unprocessable entity error at", ex)
        return ResponseStatusException(
            HttpStatus.UNPROCESSABLE_ENTITY,
            ex.message,
        )
    }

    @ExceptionHandler(value = [IllegalArgumentException::class, IllegalStateException::class])
    fun handleConflict(
        ex: RuntimeException,
        request: WebRequest,
    ): ResponseStatusException {
        log.error("Invalid state or requirements at", ex)
        return ResponseStatusException(
            HttpStatus.FAILED_DEPENDENCY,
            ex.message ?: "Invalid state or requirements",
            ex,
        )
    }

    @ExceptionHandler(value = [ChatImporterException::class, BucketServiceException::class])
    fun handleInternalServerError(
        ex: RuntimeException,
        request: WebRequest,
    ): ResponseStatusException {
        log.error("The request failed due to an unexpected error at", ex)
        return ResponseStatusException(
            HttpStatus.INTERNAL_SERVER_ERROR,
            ex.message ?: "The request failed due to an unexpected error. See the server logs for more details.",
            ex,
        )
    }
}
