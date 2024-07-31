package io.github.anki.anki.controller

import io.github.anki.testing.MVCTest
import io.github.anki.testing.getRandomString
import io.kotest.matchers.shouldBe
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito.mock
import org.slf4j.LoggerFactory
import org.springframework.core.MethodParameter
import org.springframework.http.HttpStatus
import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import java.util.stream.Stream


@MVCTest
class ControllerExceptionHandlerTest {

    private val exceptionHandler = ControllerExceptionHandler()

    @ParameterizedTest
    @MethodSource("getExceptionTypes")
    fun `should catch exception and return 500`(exception: Exception) {
        val responseEntity = exceptionHandler.globalExceptionHandler(exception)

        LOG.info("Got {} on ERROR", responseEntity)

        responseEntity.statusCode shouldBe HttpStatus.INTERNAL_SERVER_ERROR

        responseEntity.body shouldBe "Internal Server Error"

    }

    @ParameterizedTest
    @MethodSource("getMethodArgumentNotValidExceptionTestArguments")
    fun `should return 400 when MethodArgumentNotValidException`(
        objectName: String,
        fieldName: String) {
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
        fieldName: String) {
        // given
        val defaultMessage = getRandomString()

        val exception = createMethodArgumentNotValidException(objectName, fieldName, defaultMessage)

        // when
        val responseBodyMap = exceptionHandler.getErrorsFromMethodArgumentNotValidException(exception)

        // then
        responseBodyMap shouldBe linkedMapOf(fieldName to defaultMessage)

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

        private val LOG = LoggerFactory.getLogger(ControllerExceptionHandlerTest::class.java)

        @JvmStatic
        @Suppress("UnusedPrivateMember")
        private fun getExceptionTypes(): Stream<Arguments> =
            Stream.of(
                Arguments.of(Exception()),
                Arguments.of(NullPointerException()),
                Arguments.of(RuntimeException()),
                Arguments.of(IllegalStateException())
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