package io.github.anki.anki.service

import io.github.anki.anki.controller.dto.auth.SignUpRequestDto
import io.github.anki.anki.controller.dto.mapper.toUser
import io.github.anki.anki.repository.mongodb.UserRepository
import io.github.anki.anki.repository.mongodb.document.MongoUser
import io.github.anki.anki.service.exceptions.UserAlreadyExistException
import io.github.anki.anki.service.exceptions.UserDoesNotExistException
import io.github.anki.anki.service.model.User
import io.github.anki.anki.service.model.mapper.toMongoUser
import io.github.anki.anki.service.secure.SecurityService
import io.github.anki.testing.getRandomID
import io.github.anki.testing.randomUser
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.dao.DuplicateKeyException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import kotlin.test.BeforeTest
import kotlin.test.Test

@ExtendWith(MockKExtension::class)
class UserServiceTest {
    @InjectMockKs
    private lateinit var userService: UserService

    @MockK
    private lateinit var userRepository: UserRepository

    @MockK
    private lateinit var securityService: SecurityService

    private lateinit var newUserDto: SignUpRequestDto
    private lateinit var newUser: User

    private val encoder = BCryptPasswordEncoder()

    @BeforeTest
    fun setUp() {
        newUserDto = SignUpRequestDto.randomUser()
        newUser = newUserDto.toUser(encoder)
    }

    @Nested
    @DisplayName("UserService.signUp()")
    @TestInstance(Lifecycle.PER_CLASS)
    inner class CreateNewUser {
        @Test
        fun `should create new user always`() {
            // given
            val createdMongoUser = newUser.toMongoUser().copy(id = getRandomID())
            val expectedUser = newUser.copy(id = createdMongoUser.id!!.toString())
            every {
                userRepository.insert(newUser.toMongoUser())
            } returns Mono.just(createdMongoUser)

            // when/then
            StepVerifier
                .create(
                    userService.signUp(newUser),
                )
                .assertNext {
                    it shouldBe expectedUser
                }
                .verifyComplete()
        }

        @Test
        fun `should throw UserAlreadyExistException if user already exist `() {
            // given
            every {
                userRepository.insert(newUser.toMongoUser())
            } returns Mono.error(DuplicateKeyException(MongoUser.USER_NAME))

            // when/then
            StepVerifier
                .create(
                    userService.signUp(newUser),
                )
                .verifyError(UserAlreadyExistException::class.java)
        }

        @Test
        fun `should trow DuplicateKeyException if user have duplicated fields`() {
            // given
            every {
                userRepository.insert(newUser.toMongoUser())
            } returns Mono.error(DuplicateKeyException("other field"))

            // when/then
            StepVerifier
                .create(
                    userService.signUp(newUser),
                )
                .verifyError(DuplicateKeyException::class.java)
        }

        @Test
        fun `should not trow DuplicateKeyException if user already exist`() {
            // given
            every {
                userRepository.insert(newUser.toMongoUser())
            } returns Mono.error(DuplicateKeyException(MongoUser.EMAIL))

            // when/then
            StepVerifier
                .create(
                    userService.signUp(newUser),
                )
                .expectNextCount(1)
                .verifyComplete()
        }
    }

    @Nested
    @DisplayName("UserService.signIn()")
    @TestInstance(Lifecycle.PER_CLASS)
    inner class LogInUser {

        @Test
        fun `should return 400 UsernameNotFoundException user is userName not found`() {
            // given
            every {
                securityService.authUser(newUser)
            } returns Mono.error(UserDoesNotExistException.fromUserName(newUser.userName))

            // when/then
            StepVerifier
                .create(
                    userService.signIn(newUser),
                )
                .verifyError(UserDoesNotExistException::class.java)
        }
    }
}
