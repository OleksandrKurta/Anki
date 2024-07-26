package io.github.anki.anki.controller

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
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error")
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(ControllerExceptionHandler::class.java)
    }
}
