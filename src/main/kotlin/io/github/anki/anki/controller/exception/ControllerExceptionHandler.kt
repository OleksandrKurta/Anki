package io.github.anki.anki.controller.exception

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ControllerExceptionHandler {

    @ExceptionHandler(Exception::class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    fun globalExceptionHandler(
        ex: Exception,
    ): ResponseEntity<String> {
        LOG.error("Handling global exception", ex)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("InternalServerError")
    }

    @ExceptionHandler(UnauthorizedException::class)
    @ResponseStatus(value = HttpStatus.UNAUTHORIZED)
    fun unauthorizedExceptionHandler(
        ex: RuntimeException,
    ): ResponseEntity<String> {
        LOG.error("Handling unauthorized exception", ex)
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("UnauthenticatedError")
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(ControllerExceptionHandler::class.java)
    }
}
