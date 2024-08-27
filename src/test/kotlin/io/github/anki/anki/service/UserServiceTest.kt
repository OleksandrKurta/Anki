package io.github.anki.anki.service

import io.github.anki.anki.controller.dto.auth.SignUpRequestDto
import io.github.anki.anki.controller.dto.mapper.toUser
import io.github.anki.anki.repository.mongodb.UserRepository
import io.github.anki.anki.repository.mongodb.document.MongoUser
import io.github.anki.anki.service.exceptions.UserAlreadyExistException
import io.github.anki.anki.service.exceptions.UserDoesNotExistException
import io.github.anki.anki.service.model.User
import io.github.anki.anki.service.model.mapper.toMongoUser
import io.github.anki.testing.getRandomID
import io.github.anki.testing.randomUser
import io.kotest.assertions.throwables.shouldNotThrowExactly
import io.kotest.assertions.throwables.shouldThrowExactly
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
import kotlin.test.BeforeTest
import kotlin.test.Test

@ExtendWith(MockKExtension::class)
class UserServiceTest {
    @InjectMockKs
    lateinit var userService: UserService

    @MockK
    lateinit var userRepository: UserRepository
    private lateinit var newUser: SignUpRequestDto
    private lateinit var encodedPassword: String

    @BeforeTest
    fun setUp() {
        newUser = SignUpRequestDto.randomUser()
        encodedPassword = BCryptPasswordEncoder().encode(newUser.password)
    }

    @Nested
    @DisplayName("UserService.signUp()")
    @TestInstance(Lifecycle.PER_CLASS)
    inner class CreateNewUser {
        @Test
        fun `should create new user always`() {
            // when
            val user = newUser.toUser(encodedPassword)
            val createdMongoUser = user.toMongoUser().copy(id = getRandomID())
            val expectedUser = user.copy(id = createdMongoUser.id!!.toHexString())
            every { userRepository.insert(user.toMongoUser()) } returns createdMongoUser
            val actualUser: User? = userService.signUp(newUser.toUser(encodedPassword))

            // then

            actualUser shouldBe expectedUser
        }

        @Test
        fun `should throw UserAlreadyExistException if user already exist `() {
            // given
            val user = newUser.toUser(encodedPassword)

            every { userRepository.insert(user.toMongoUser()) } throws DuplicateKeyException(MongoUser.USER_NAME)
            // then
            shouldThrowExactly<UserAlreadyExistException> {
                userService.signUp(newUser.toUser(encodedPassword))
            }
        }

        @Test
        fun `should trow DuplicateKeyException if user have duplicated fields`() {
            // given
            val user = newUser.toUser(encodedPassword)

            every { userRepository.insert(user.toMongoUser()) } throws DuplicateKeyException("other field")
            // then

            shouldThrowExactly<DuplicateKeyException> {
                userService.signUp(newUser.toUser(encodedPassword))
            }
        }

        @Test
        fun `should not trow DuplicateKeyException if user already exist `() {
            // given
            val user = newUser.toUser(encodedPassword)

            every { userRepository.insert(user.toMongoUser()) } throws DuplicateKeyException(MongoUser.EMAIL)
            // then

            shouldNotThrowExactly<DuplicateKeyException> {
                userService.signUp(newUser.toUser(encodedPassword))
            }
        }
    }

    @Nested
    @DisplayName("UserService.signIn()")
    @TestInstance(Lifecycle.PER_CLASS)
    inner class LogInUser {

        @Test
        fun `should return 400 UserDoesNotExistException user always`() {
            // then
            val user = newUser.toUser(encodedPassword)
            every { userRepository.existsByUserName(user.userName) } returns false

            shouldThrowExactly<UserDoesNotExistException> {
                userService.signIn(user)
            }
        }
    }
}
