package io.github.anki.anki.service.secure

import io.github.anki.anki.controller.dto.auth.SignUpRequestDto
import io.github.anki.anki.controller.dto.mapper.toUser
import io.github.anki.anki.service.exceptions.UserDoesNotExistException
import io.github.anki.anki.service.model.User
import io.github.anki.anki.service.secure.jwt.JwtUtils
import io.github.anki.testing.getRandomID
import io.github.anki.testing.getRandomString
import io.github.anki.testing.randomUser
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import kotlin.test.Test

@ExtendWith(MockKExtension::class)
class AuthenticationManagerTest {

    @MockK
    private lateinit var userDetailsService: UserDetailsService

    @MockK
    private lateinit var jwtUtils: JwtUtils

    @InjectMockKs
    private lateinit var authenticationManager: AuthenticationManager

    private lateinit var user: User
    private val encoder = BCryptPasswordEncoder()

    @BeforeEach
    fun setUp() {
        user = SignUpRequestDto.randomUser().toUser(encoder).copy(id = getRandomID().toString())
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `should authenticate user always`() {
        // given
        val token = getRandomString()

        every {
            jwtUtils.generateJwtToken(any<User>())
        } returns token
        every {
            userDetailsService.findByUsername(user.userName!!)
        } returns Mono.just(user)

        // when/then
        StepVerifier
            .create(
                authenticationManager.authenticate(user),
            )
            .expectNext(
                UserAuthentication(user, token),
            )
            .verifyComplete()

        verify(exactly = 1) {
            userDetailsService.findByUsername(user.userName!!)
        }
        verify(exactly = 1) {
            jwtUtils.generateJwtToken(user)
        }
    }

    @Test
    fun `should not authenticate user if was not found in db`() {
        // given
        every {
            userDetailsService.findByUsername(user.userName!!)
        } returns Mono.error(UserDoesNotExistException.fromUserName(user.userName))

        // when/then
        StepVerifier
            .create(
                authenticationManager.authenticate(user),
            )
            .verifyError(UserDoesNotExistException::class.java)

        verify(exactly = 1) {
            userDetailsService.findByUsername(user.userName!!)
        }

        verify(exactly = 0) {
            jwtUtils.generateJwtToken(any())
        }
    }
}
