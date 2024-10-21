package io.github.anki.anki.controller

import io.github.anki.anki.service.exceptions.BaseBadRequestException
import io.github.anki.anki.service.exceptions.CardDoesNotExistException
import io.github.anki.anki.service.exceptions.DeckDoesNotExistException
import io.github.anki.anki.service.exceptions.UserAlreadyExistException
import io.github.anki.anki.service.exceptions.UserDoesNotExistException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import reactor.core.publisher.Mono

@RestControllerAdvice
class ControllerExceptionHandler {
    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun methodNotSupportedHandler(): Mono<ResponseEntity<String>> =
        Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not Found"))

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(ex: MethodArgumentNotValidException): Mono<ResponseEntity<Map<String, String>>> =
        Mono.just(ResponseEntity(ex.toMap(), HttpStatus.BAD_REQUEST))
//
//    @ExceptionHandler(CardDoesNotExistException::class)
//    fun cardDoesNotExistHandler(ex: BaseBadRequestException): Mono<ResponseEntity<String>> {
//        LOG.error("OUT CardsController ${CardsController.BASE_URL} ${ex.message}")
//        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.message))
//    }
//
//    @ExceptionHandler(DeckDoesNotExistException::class)
//    fun deckDoesNotExistHandler(ex: BaseBadRequestException): Mono<ResponseEntity<String>> {
//        LOG.error("OUT DecksController ${DecksController.BASE_URL} ${ex.message}")
//        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.message))
//    }

    @ExceptionHandler(UserDoesNotExistException::class)
    fun userDoesNotExistHandler(ex: BaseBadRequestException): Mono<ResponseEntity<String>> {
        LOG.error("OUT AuthController ${AuthController.BASE_URL} ${AuthController.SIGN_UP} ${ex.message}")
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.message))
    }

    @ExceptionHandler(UserAlreadyExistException::class)
    fun hasAlreadyExistHandler(ex: BaseBadRequestException): Mono<ResponseEntity<String>> {
        LOG.error("OUT AuthController ${AuthController.BASE_URL} ${AuthController.SIGN_UP} ${ex.message}")
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.message))
    }

    @ExceptionHandler(Exception::class)
    fun globalExceptionHandler(
        ex: Exception,
    ): Mono<ResponseEntity<String>> {
        LOG.error("Handling global exception", ex)
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error"))
    }

    private fun MethodArgumentNotValidException.toMap(): Map<String, String> {
        return bindingResult.fieldErrors
            .associate { it.field to (it.defaultMessage ?: "Validation error") }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(ControllerExceptionHandler::class.java)
    }
}
