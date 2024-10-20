package io.github.anki.anki.controller

import io.github.anki.anki.service.exceptions.CardDoesNotExistException
import io.github.anki.anki.service.exceptions.DeckDoesNotExistException
import io.github.anki.anki.service.exceptions.UserAlreadyExistException
import io.github.anki.anki.service.exceptions.UserDoesNotExistException
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito.mock
import org.springframework.core.MethodParameter
import org.springframework.http.HttpStatus
import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import reactor.test.StepVerifier
import java.util.stream.Stream
import kotlin.test.Test

@TestInstance(Lifecycle.PER_CLASS)
class ControllerExceptionHandlerTest {
    private val exceptionHandler = ControllerExceptionHandler()

    private val notBlankDefaultMessage = "must not be blank"

    @ParameterizedTest
    @MethodSource("getExceptionTypes")
    fun `should catch exception and return 500`(exception: Exception) {
        // when
        StepVerifier
            .create(exceptionHandler.globalExceptionHandler(exception))
            .assertNext {
                it.statusCode shouldBe HttpStatus.INTERNAL_SERVER_ERROR
                it.body shouldBe "Internal Server Error"
            }
            .expectComplete()
            .verify()
    }

    @ParameterizedTest
    @MethodSource("getMethodArgumentNotValidExceptionTestArguments")
    fun `should return 400 when MethodArgumentNotValidException`(
        objectName: String,
        fieldName: String,
    ) {
        val exception = createMethodArgumentNotValidException(objectName, fieldName)
        StepVerifier
            .create(exceptionHandler.handleValidationExceptions(exception))
            .assertNext {
                it.statusCode shouldBe HttpStatus.BAD_REQUEST
                it.body shouldBe mapOf(fieldName to notBlankDefaultMessage)
            }
            .expectComplete()
            .verify()
    }

    @Test
    fun `should return 404 if method or path not found`() {
        StepVerifier
            .create(exceptionHandler.methodNotSupportedHandler())
            .assertNext {
                it.statusCode shouldBe HttpStatus.NOT_FOUND
                it.body shouldBe "Not Found"
            }
            .expectComplete()
            .verify()
    }

    @Test
    fun `should return 400 if user does not have such deck`() {
        StepVerifier
            .create(exceptionHandler.deckDoesNotExistHandler(DeckDoesNotExistException()))
            .assertNext {
                it.statusCode shouldBe HttpStatus.BAD_REQUEST
                it.body shouldBe "Deck does not exist"
            }
            .expectComplete()
            .verify()
    }

    @Test
    fun `should return 400 if user does not have such card`() {
        StepVerifier
            .create(exceptionHandler.cardDoesNotExistHandler(CardDoesNotExistException()))
            .assertNext {
                it.statusCode shouldBe HttpStatus.BAD_REQUEST
                it.body shouldBe "Card does not exist"
            }
            .expectComplete()
            .verify()
    }

    @Test
    fun `should return 400 if user not found`() {
        StepVerifier
            .create(exceptionHandler.userDoesNotExistHandler(UserDoesNotExistException()))
            .assertNext {
                it.statusCode shouldBe HttpStatus.BAD_REQUEST
                it.body shouldBe "User does not exist"
            }
            .expectComplete()
            .verify()
    }

    @Test
    fun `should return 400 if user has already exist`() {
        StepVerifier
            .create(exceptionHandler.hasAlreadyExistHandler(UserAlreadyExistException()))
            .assertNext {
                it.statusCode shouldBe HttpStatus.BAD_REQUEST
                it.body shouldBe "User already exists"
            }
            .expectComplete()
            .verify()
    }

    @Suppress("UnusedPrivateMember")
    private fun getExceptionTypes(): Stream<Arguments> {
        return Stream.of(
            Arguments.of(Exception()),
            Arguments.of(NullPointerException()),
            Arguments.of(RuntimeException()),
            Arguments.of(IllegalStateException()),
        )
    }

    @Suppress("UnusedPrivateMember")
    private fun getMethodArgumentNotValidExceptionTestArguments(): Stream<Arguments> =
        Stream.of(
            Arguments.of("NewCardRequest", "deckId"),
            Arguments.of("NewDeckRequest", "name"),
        )

    private fun createMethodArgumentNotValidException(
        objectName: String,
        fieldName: String,
    ): MethodArgumentNotValidException {
        val fieldError = FieldError(objectName, fieldName, notBlankDefaultMessage)
        val methodParameter = mock(MethodParameter::class.java)

        val bindingResult = BeanPropertyBindingResult(null, objectName)
        bindingResult.addError(fieldError)

        return MethodArgumentNotValidException(methodParameter, bindingResult)
    }
}
