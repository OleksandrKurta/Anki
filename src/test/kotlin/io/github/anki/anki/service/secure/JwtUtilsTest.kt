package io.github.anki.anki.service.secure

import io.github.anki.anki.service.secure.jwt.JwtUtils
import io.github.anki.testing.getRandomString
import io.kotest.assertions.throwables.shouldThrowExactly
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.Test

@ExtendWith(MockKExtension::class)
class JwtUtilsTest {
    @InjectMockKs
    private lateinit var jwtUtils: JwtUtils

    @Nested
    @DisplayName("jwtUtils.validateJwtToken()")
    @TestInstance(Lifecycle.PER_CLASS)
    inner class JwtUtilsTest {
        @Test
        fun `should throw error while validateJwtToken`() {
            // when
            shouldThrowExactly<IllegalArgumentException> {
                jwtUtils.validateJwtToken(getRandomString())
            }
        }

        @Test
        fun `should throw exception`() {
            // when
            shouldThrowExactly<IllegalArgumentException> {
                jwtUtils.getAuthFromAuthHeader(header = org.springframework.http.HttpHeaders())
            }
        }
    }
}
