package io.github.anki.anki.service

import io.github.anki.anki.controller.dto.auth.SignUpRequestDto
import io.github.anki.anki.controller.dto.mapper.toUser
import io.github.anki.anki.repository.mongodb.UserRepository
import io.github.anki.anki.service.exceptions.UserAlreadyExistException
import io.github.anki.anki.service.exceptions.UserDoesNotExistException
import io.github.anki.anki.service.model.User
import io.github.anki.testing.MVCTest
import io.github.anki.testing.randomUser
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.bson.types.ObjectId
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.password.PasswordEncoder
import kotlin.test.BeforeTest
import kotlin.test.Test

@MVCTest
class UserServiceTest @Autowired constructor(
    var userService: UserService,
    var userRepository: UserRepository,
    var passwordEncoder: PasswordEncoder,
) {
    private lateinit var newUser: SignUpRequestDto
    private lateinit var encodedPassword: String
    private lateinit var expectedUser: User

    @BeforeTest
    fun setUp() {
        newUser = SignUpRequestDto.randomUser()
        encodedPassword = passwordEncoder.encode(newUser.password)
        expectedUser =
            User(
                userName = newUser.userName,
                email = newUser.email,
                password = newUser.password,
                authorities = listOf(),
            )
    }

    @Nested
    @DisplayName("UserService.signUp()")
    @TestInstance(Lifecycle.PER_CLASS)
    inner class CreateNewUser {
        @Test
        fun `should create new user always`() {
            // when
            val actualUser: User? = userService.signUp(newUser.toUser(encodedPassword))

            // then
            actualUser!!.id shouldNotBe null
            userRepository.existsById(ObjectId(actualUser.id)) shouldBe true
            actualUser.id = null

            actualUser shouldBe expectedUser
        }

        @Test
        fun `should return 400 if user already exist `() {
            // given
            val givenUser = userService.signUp(newUser.toUser(encodedPassword))
            givenUser shouldNotBe null
            // then
            shouldThrowExactly<UserAlreadyExistException> {
                userService.signUp(newUser.toUser(encodedPassword))
            }
        }
    }

    @Nested
    @DisplayName("UserService.signIn()")
    @TestInstance(Lifecycle.PER_CLASS)
    inner class LogInUser {
        @Test
        fun `should log in user always`() {
            // when
            val actualUser: User = userService.signIn(newUser.toUser(encodedPassword))
            // then
            actualUser.id = null
            actualUser shouldBe expectedUser
        }

        @Test
        fun `should return 400 UserDoesNotExistException user always`() {
            // then
            shouldThrowExactly<UserDoesNotExistException> {
                userService.signIn(SignUpRequestDto.randomUser().toUser(encodedPassword))
            }
        }
    }
}
