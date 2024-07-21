package io.github.anki.anki.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import java.util.*


@RestControllerAdvice
class ControllerExceptionHandler {


    @ExceptionHandler(ResourceNotFoundException::class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    fun resourceNotFoundException(ex: ResourceNotFoundException, request: WebRequest): ErrorMessage {
        val message = ErrorMessage(
            HttpStatus.NOT_FOUND.value(),
            Date(),
            ex.message!!,
            request.getDescription(false)
        )

        return message
    }

    @ExceptionHandler(Exception::class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    fun globalExceptionHandler(ex: Exception, request: WebRequest): ErrorMessage {
        val message = ErrorMessage(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            Date(),
            ex.message!!,
            request.getDescription(false)
        )

        return message
    }
}
