package io.github.anki.anki.controller

import io.github.anki.anki.controller.exceptions.CardDoesNotExistException
import io.github.anki.anki.controller.exceptions.DeckDoesNotExistException
import io.github.anki.testing.MVCTest
import io.github.anki.testing.getRandomString
import io.kotest.matchers.shouldBe
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

@MVCTest
class ControllerExceptionHandlerTest {
    private val exceptionHandler = ControllerExceptionHandler()

    @ParameterizedTest
    @MethodSource("getExceptionTypes")
    fun `should catch exception and return 500`(exception: Exception) {
        val responseEntity = exceptionHandler.globalExceptionHandler(exception)

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
        val defaultMessage = "should not be blank"

        val exception = createMethodArgumentNotValidException(objectName, fieldName, defaultMessage)

        // when
        val responseEntity = exceptionHandler.handleValidationExceptions(exception)

        // then
        responseEntity.statusCode shouldBe HttpStatus.BAD_REQUEST

        responseEntity.body shouldBe linkedMapOf(fieldName to defaultMessage)
    }

    @ParameterizedTest
    @MethodSource("getMethodArgumentNotValidExceptionTestArguments")
    fun `should return right map from MethodArgumentNotValidException`(
        objectName: String,
        fieldName: String,
    ) {
        // given
        val defaultMessage = getRandomString()

        val exception = createMethodArgumentNotValidException(objectName, fieldName, defaultMessage)

        // when
        val responseBodyMap = exceptionHandler.getValidationException(exception)

        // then
        responseBodyMap shouldBe linkedMapOf(fieldName to defaultMessage)
    }

    @Test
    fun `should return 404 if method or path not found`() {
        val responseEntity = exceptionHandler.methodNotSupportedHandler()

        responseEntity.statusCode shouldBe HttpStatus.NOT_FOUND

        responseEntity.body shouldBe "Not Found"
    }

    @Test
    fun `should return 400 if user does not have such deck`() {
        val responseEntity = exceptionHandler.doesNotExistHandler(DeckDoesNotExistException())

        responseEntity.statusCode shouldBe HttpStatus.BAD_REQUEST

        responseEntity.body shouldBe "Deck does not exist"
    }

    @Test
    fun `should return 400 if user does not have such card`() {
        val responseEntity = exceptionHandler.doesNotExistHandler(CardDoesNotExistException())

        responseEntity.statusCode shouldBe HttpStatus.BAD_REQUEST

        responseEntity.body shouldBe "Card does not exist"
    }

    private fun createMethodArgumentNotValidException(
        objectName: String,
        fieldName: String,
        defaultMessage: String,
    ): MethodArgumentNotValidException {
        val fieldError = FieldError(objectName, fieldName, defaultMessage)
        val methodParameter = mock(MethodParameter::class.java)

        val bindingResult = BeanPropertyBindingResult(null, objectName)
        bindingResult.addError(fieldError)

        return MethodArgumentNotValidException(methodParameter, bindingResult)
    }

    companion object {
        @JvmStatic
        @Suppress("UnusedPrivateMember")
        private fun getExceptionTypes(): Stream<Arguments> =
            Stream.of(
                Arguments.of(Exception()),
                Arguments.of(NullPointerException()),
                Arguments.of(RuntimeException()),
                Arguments.of(IllegalStateException()),
            )

        @JvmStatic
        @Suppress("UnusedPrivateMember")
        private fun getMethodArgumentNotValidExceptionTestArguments(): Stream<Arguments> =
            Stream.of(
                Arguments.of("NewCardRequest", "deckId"),
                Arguments.of("NewDeckRequest", "name"),
            )
    }
}
