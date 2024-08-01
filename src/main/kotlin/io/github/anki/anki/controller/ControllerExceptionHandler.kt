package io.github.anki.anki.controller

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.util.LinkedHashMap

@RestControllerAdvice
class ControllerExceptionHandler {

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun methodNotSupportedHandler(): ResponseEntity<String> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not Found")

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(ex: MethodArgumentNotValidException): ResponseEntity<Map<String, String>> {
        return ResponseEntity(getErrorsFromMethodArgumentNotValidException(ex), HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(DeckDoesNotExistException::class)
    fun deckDoesNotExistHandler(ex: DeckDoesNotExistException): ResponseEntity<String> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.message)
    }

    @ExceptionHandler(Exception::class)
    fun globalExceptionHandler(
        ex: Exception,
    ): ResponseEntity<String> {
        LOG.error("Handling global exception", ex)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error")
    }


    fun getErrorsFromMethodArgumentNotValidException(
        ex: MethodArgumentNotValidException): LinkedHashMap<String, String>? {
        val firstValidationError: FieldError? = ex.bindingResult.fieldError
        return linkedMapOf(firstValidationError!!.field to firstValidationError.defaultMessage!!)
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(ControllerExceptionHandler::class.java)
    }
}
