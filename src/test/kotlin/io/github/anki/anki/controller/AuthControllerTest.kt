package io.github.anki.anki.controller

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.anki.anki.controller.AuthController.Companion.BASE_URL
import io.github.anki.anki.controller.AuthController.Companion.CREATED_USER_MESSAGE
import io.github.anki.anki.controller.AuthController.Companion.SIGN_IN
import io.github.anki.anki.controller.AuthController.Companion.SIGN_UP
import io.github.anki.anki.controller.dto.auth.JwtResponseDto
import io.github.anki.anki.controller.dto.auth.SignInRequestDto
import io.github.anki.anki.controller.dto.auth.SignUpRequestDto
import io.github.anki.anki.controller.dto.auth.UserCreatedMessageResponseDto
import io.github.anki.anki.controller.dto.mapper.toUser
import io.github.anki.anki.repository.mongodb.UserRepository
import io.github.anki.anki.service.exceptions.UserAlreadyExistException
import io.github.anki.anki.service.exceptions.UserDoesNotExistException
import io.github.anki.anki.service.model.mapper.toMongoUser
import io.github.anki.anki.service.model.mapper.toUser
import io.github.anki.anki.service.secure.AuthenticationManager
import io.github.anki.testing.IntegrationTestWithClient
import io.github.anki.testing.getRandomEmail
import io.github.anki.testing.getRandomString
import io.github.anki.testing.randomUser
import io.github.anki.testing.testcontainers.TestContainersFactory
import io.github.anki.testing.testcontainers.with
import io.kotest.matchers.equality.shouldBeEqualToComparingFields
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Container
import reactor.test.StepVerifier
import java.nio.charset.StandardCharsets
import kotlin.test.BeforeTest

@IntegrationTestWithClient
class AuthControllerTest @Autowired constructor(
    private val objectMapper: ObjectMapper,
    private val userRepository: UserRepository,
    private val encoder: PasswordEncoder,
    private val authenticationManager: AuthenticationManager,
    private val webTestClient: WebTestClient,
) {
    private lateinit var newUserDto: SignUpRequestDto
    private lateinit var token: String

    @BeforeTest
    fun setUp() {
        newUserDto = SignUpRequestDto.randomUser()
        val newUser = newUserDto.toUser(encoder)
        userRepository.insert(newUser.toMongoUser()).block()
        token = authenticationManager
            .authenticate(newUser)
            .map { it.creds }
            .block()!!
    }

    @Nested
    @DisplayName("POST $BASE_URL$SIGN_IN")
    @TestInstance(Lifecycle.PER_CLASS)
    inner class PostSignInUser {

        @Test
        fun `should authenticate User always`() {
            signInUser(SignInRequestDto(newUserDto.userName, newUserDto.password))
                .expectStatus()
                .isOk
                .expectHeader()
                .contentType(MediaType.APPLICATION_JSON)
                .expectBody(JwtResponseDto::class.java)
        }

        @Test
        fun `should return 400 if user does not exist`() {
            // given
            val randomUserName = getRandomString()
            signInUser(SignInRequestDto(randomUserName, newUserDto.password))
                .expectStatus()
                .isBadRequest
                .expectHeader()
                .contentType(MediaType(MediaType.TEXT_PLAIN, StandardCharsets.UTF_8))
                .expectBody(String::class.java)
                .isEqualTo(UserDoesNotExistException.fromUserName(randomUserName).message)
        }

        private fun signInUser(signInUserRequest: SignInRequestDto): WebTestClient.ResponseSpec =
            webTestClient
                .post()
                .uri(BASE_URL + SIGN_IN)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(objectMapper.writeValueAsString(signInUserRequest))
                .exchange()
    }

    @Nested
    @DisplayName("POST $BASE_URL$SIGN_UP")
    @TestInstance(Lifecycle.PER_CLASS)
    inner class PostSignUpUser {

        @Test
        fun `should create User always`() {
            // given
            val randomUser: SignUpRequestDto = SignUpRequestDto.randomUser()

            // when
            val response: WebTestClient.ResponseSpec = signUpUser(randomUser)

            // then
            response
                .expectStatus()
                .isCreated
                .expectHeader()
                .contentType(MediaType.APPLICATION_JSON)
                .expectBody(UserCreatedMessageResponseDto::class.java)
                .isEqualTo(UserCreatedMessageResponseDto(CREATED_USER_MESSAGE))
            StepVerifier
                .create(userRepository.findByUserName(randomUser.userName!!))
                .assertNext {
                    it.toUser().apply { id = null } shouldBeEqualToComparingFields
                        randomUser.toUser(encoder)
                }
                .verifyComplete()
        }

        @Test
        fun `should return 200 if user with email exist but and doesn't create one`() {
            // given
            val randomUserName = getRandomString()

            // when
            val response =
                signUpUser(
                    SignUpRequestDto(
                        userName = randomUserName,
                        email = newUserDto.email,
                        password = newUserDto.password,
                        roles = setOf(),
                    ),
                )

            // then
            response
                .expectStatus()
                .isCreated
                .expectHeader()
                .contentType(MediaType.APPLICATION_JSON)
                .expectBody(String::class.java)
                .isEqualTo(objectMapper.writeValueAsString(UserCreatedMessageResponseDto(CREATED_USER_MESSAGE)))

            userRepository.existsByUserName(userName = randomUserName).block() shouldBe false
        }

        @Test
        fun `should return 400 if user with userName exist`() {
            // when
            val response =
                signUpUser(
                    SignUpRequestDto(
                        userName = newUserDto.userName,
                        email = getRandomEmail("initial"),
                        password = newUserDto.password,
                        roles = setOf(),
                    ),
                )

            // then
            response
                .expectStatus()
                .isBadRequest
                .expectHeader()
                .contentType(MediaType(MediaType.TEXT_PLAIN, StandardCharsets.UTF_8))
                .expectBody(String::class.java)
                .isEqualTo(UserAlreadyExistException.fromUserName(newUserDto.userName).message)
        }

        private fun signUpUser(signUpUserRequest: SignUpRequestDto): WebTestClient.ResponseSpec =
            webTestClient
                .post()
                .uri(BASE_URL + SIGN_UP)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(objectMapper.writeValueAsString(signUpUserRequest))
                .exchange()
    }
    companion object {
//        private val LOG: Logger = LoggerFactory.getLogger(AuthControllerTest::class.java)

        @Container
        @Suppress("PropertyName")
        private val mongoDBContainer: MongoDBContainer = TestContainersFactory.newMongoContainer()

        @DynamicPropertySource
        @JvmStatic
        fun setMongoUri(registry: DynamicPropertyRegistry) {
            registry.with(mongoDBContainer)
        }
    }
}
