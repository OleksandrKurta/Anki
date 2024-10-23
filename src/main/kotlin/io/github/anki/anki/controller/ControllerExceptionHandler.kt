package io.github.anki.anki.controller

import io.github.anki.anki.service.exceptions.CardDoesNotExistException
import io.github.anki.anki.service.exceptions.DeckDoesNotExistException
import io.github.anki.anki.service.exceptions.UserAlreadyExistException
import io.github.anki.anki.service.exceptions.UserDoesNotExistException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.support.WebExchangeBindException
import reactor.core.publisher.Mono

@RestControllerAdvice
class ControllerExceptionHandler {
    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun methodNotSupportedHandler(): Mono<ResponseEntity<String>> =
        Mono
            .just(ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not Found"))

    @ExceptionHandler(WebExchangeBindException::class)
    fun handleValidationExceptions(ex: WebExchangeBindException): Mono<ResponseEntity<Map<String, String>>> =
        Mono
            .just(ResponseEntity(ex.toMap(), HttpStatus.BAD_REQUEST))
            .doOnNext { LOG.error("Handling WebExchangeBindException exception with message {}", it.body) }

    @ExceptionHandler(CardDoesNotExistException::class)
    fun cardDoesNotExistHandler(ex: CardDoesNotExistException): Mono<ResponseEntity<String>> =
        Mono
            .just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.message))
            .doFirst { LOG.error("OUT CardsController ${CardsController.BASE_URL} ${ex.message}") }

    @ExceptionHandler(DeckDoesNotExistException::class)
    fun deckDoesNotExistHandler(ex: DeckDoesNotExistException): Mono<ResponseEntity<String>> =
        Mono
            .just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.message))
            .doFirst { LOG.error("OUT DecksController ${DecksController.BASE_URL} ${ex.message}") }

    @ExceptionHandler(UserDoesNotExistException::class)
    fun userDoesNotExistHandler(ex: UserDoesNotExistException): Mono<ResponseEntity<String>> =
        Mono
            .just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.message))
            .doFirst {
                LOG.error("OUT AuthController ${AuthController.BASE_URL} ${AuthController.SIGN_UP} ${ex.message}")
            }

    @ExceptionHandler(UserAlreadyExistException::class)
    fun hasAlreadyExistHandler(ex: UserAlreadyExistException): Mono<ResponseEntity<String>> =
        Mono
            .just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.message))
            .doFirst {
                LOG.error("OUT AuthController ${AuthController.BASE_URL} ${AuthController.SIGN_UP} ${ex.message}")
            }

    @ExceptionHandler(Exception::class)
    fun globalExceptionHandler(
        ex: Exception,
    ): Mono<ResponseEntity<String>> =
        Mono
            .just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error"))
            .doFirst { LOG.error("Handling global exception {} {}", ex::class.java, ex.message) }

    private fun WebExchangeBindException.toMap(): Map<String, String> {
        return bindingResult.fieldErrors
            .associate { it.field to (it.defaultMessage ?: "Validation error") }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(ControllerExceptionHandler::class.java)
    }
}
