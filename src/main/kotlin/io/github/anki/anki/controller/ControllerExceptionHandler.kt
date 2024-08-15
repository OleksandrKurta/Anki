package io.github.anki.anki.controller

import io.github.anki.anki.service.exceptions.CardDoesNotExistException
import io.github.anki.anki.service.exceptions.DeckDoesNotExistException
import io.github.anki.anki.service.exceptions.DoesNotExist
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest

@RestControllerAdvice
class ControllerExceptionHandler {
    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun methodNotSupportedHandler(): ResponseEntity<String> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not Found")

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(ex: MethodArgumentNotValidException): ResponseEntity<Map<String, String>> =
        ResponseEntity(ex.toMap(), HttpStatus.BAD_REQUEST)

    @ExceptionHandler(DeckDoesNotExistException::class, CardDoesNotExistException::class)
    fun doesNotExistHandler(ex: DoesNotExist): ResponseEntity<String> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.message)

    @ExceptionHandler(Exception::class)
    fun globalExceptionHandler(
        ex: Exception,
        request: WebRequest
    ): ResponseEntity<String> {
        LOG.error("Handling global exception", ex)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error")
    }

    private fun MethodArgumentNotValidException.toMap(): Map<String, String> {
        return bindingResult.fieldErrors
            .associate { it.field to (it.defaultMessage ?: "Validation error") }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(ControllerExceptionHandler::class.java)
    }
}
