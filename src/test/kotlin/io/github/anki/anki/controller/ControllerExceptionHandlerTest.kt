package io.github.anki.anki.controller

import io.github.anki.anki.service.exceptions.CardDoesNotExistException
import io.github.anki.anki.service.exceptions.DeckDoesNotExistException
import io.github.anki.anki.service.exceptions.UserAlreadyExistException
import io.github.anki.anki.service.exceptions.UserDoesNotExistException
import io.github.anki.testing.MVCTest
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
        val responseEntity = exceptionHandler.globalExceptionHandler(exception)

        // then
        responseEntity.statusCode shouldBe HttpStatus.INTERNAL_SERVER_ERROR

        responseEntity.body shouldBe "Internal Server Error"
    }

    @ParameterizedTest
    @MethodSource("getMethodArgumentNotValidExceptionTestArguments")
    fun `should return 400 when MethodArgumentNotValidException`(
        objectName: String,
        fieldName: String,
    ) {
        // given
        val exception = createMethodArgumentNotValidException(objectName, fieldName)

        // when
        val responseEntity = exceptionHandler.handleValidationExceptions(exception)

        // then
        responseEntity.statusCode shouldBe HttpStatus.BAD_REQUEST

        responseEntity.body shouldBe mapOf(fieldName to notBlankDefaultMessage)
    }

    @Test
    fun `should return 404 if method or path not found`() {
        // when
        val responseEntity = exceptionHandler.methodNotSupportedHandler()

        // then
        responseEntity.statusCode shouldBe HttpStatus.NOT_FOUND

        responseEntity.body shouldBe "Not Found"
    }

    @Test
    fun `should return 400 if user does not have such deck`() {
        // when
        val responseEntity = exceptionHandler.deckDoesNotExistHandler(DeckDoesNotExistException())

        // then
        responseEntity.statusCode shouldBe HttpStatus.BAD_REQUEST

        responseEntity.body shouldBe "Deck does not exist"
    }

    @Test
    fun `should return 400 if user does not have such card`() {
        // when
        val responseEntity = exceptionHandler.cardDoesNotExistHandler(CardDoesNotExistException())

        // then
        responseEntity.statusCode shouldBe HttpStatus.BAD_REQUEST

        responseEntity.body shouldBe "Card does not exist"
    }

    @Test
    fun `should return 400 if user not found`() {
        // when
        val responseEntity = exceptionHandler.userDoesNotExistHandler(UserDoesNotExistException())

        // then
        responseEntity.statusCode shouldBe HttpStatus.BAD_REQUEST

        responseEntity.body shouldBe "User does not exist"
    }

    @Test
    fun `should return 400 if user has already exist`() {
        // when
        val responseEntity = exceptionHandler.hasAlreadyExistHandler(UserAlreadyExistException())

        // then
        responseEntity.statusCode shouldBe HttpStatus.BAD_REQUEST

        responseEntity.body shouldBe "User has already exist"
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
