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
import io.github.anki.anki.repository.mongodb.document.MongoUser
import io.github.anki.anki.service.exceptions.UserAlreadyExistException
import io.github.anki.anki.service.exceptions.UserDoesNotExistException
import io.github.anki.anki.service.model.User
import io.github.anki.anki.service.model.mapper.toMongoUser
import io.github.anki.anki.service.model.mapper.toUser
import io.github.anki.anki.service.secure.jwt.JwtUtils
import io.github.anki.testing.IntegrationTest
import io.github.anki.testing.ReactiveIntegrationTest
import io.github.anki.testing.getRandomString
import io.github.anki.testing.randomUser
import io.github.anki.testing.testcontainers.TestContainersFactory
import io.github.anki.testing.testcontainers.with
import io.kotest.matchers.equality.shouldBeEqualToComparingFields
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.bson.types.ObjectId
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.http.MediaType
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Container
import kotlin.test.BeforeTest

@ReactiveIntegrationTest
class AuthControllerTest @Autowired constructor(
    private val userRepository: UserRepository,
    private val jwtUtil: JwtUtils,
    private val authenticationManager: ReactiveAuthenticationManager,
) {

    @Autowired
    lateinit var webTestClient: WebTestClient

    private lateinit var objectMapper: ObjectMapper

    private lateinit var newUser: SignUpRequestDto
    private lateinit var token: String
    private val encoder: BCryptPasswordEncoder = BCryptPasswordEncoder()

    @BeforeTest
    fun setUp() {
        objectMapper = ObjectMapper()
        newUser = SignUpRequestDto.randomUser()
        val user: User = newUser.toUser(encoder.encode(newUser.password))
        userRepository.insert(user.toMongoUser()).block()
        authenticationManager
            .authenticate(UsernamePasswordAuthenticationToken(user.userName, newUser.password))
            .then()
        token = jwtUtil.generateJwtToken(user)
    }

    @Nested
    @DisplayName("POST $BASE_URL$SIGN_IN")
    @TestInstance(Lifecycle.PER_CLASS)
    inner class PostSignInUser {

        @Test
        fun `should authenticate User always`() {
            signInUser(SignInRequestDto(newUser.userName, newUser.password))
                .expectStatus().isOk
                .expectBody(JwtResponseDto::class.java)
        }
//
//        @Test
//        fun `should return 400 if user does not exist`() {
//            // given
//            val randomUserName = getRandomString()
//
//            // when
//            val performPost = signInUser(SignInRequestDto(randomUserName, newUser.password))
//            val result =
//                performPost
//                    .andDo { print() }
//                    .andExpect { status { isBadRequest() } }
//                    .andReturn()
//            // then
//            result.response.contentAsString shouldBe
//                UserDoesNotExistException.fromUserName(randomUserName).message
        }
//
        private fun signInUser(signInUserRequest: SignInRequestDto): WebTestClient. ResponseSpec =
            webTestClient
                .post()
                .uri(BASE_URL + SIGN_IN)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(objectMapper.writeValueAsString(signInUserRequest))
                .exchange()
//    }
//
//    @Nested
//    @DisplayName("POST $BASE_URL$SIGN_UP")
//    @TestInstance(Lifecycle.PER_CLASS)
//    inner class PostSignUpUser {
//
//        @Test
//        fun `should create User always`() {
//            // when
//            val randomUser = SignUpRequestDto.randomUser()
//            val performPost = signUpUser(randomUser)
//
//            val createdUserResponse =
//                performPost.andReturn()
//                    .response
//                    .contentAsString
//                    .let { objectMapper.readValue(it, UserCreatedMessageResponseDto::class.java) }
//
//            // then
//            performPost
//                .andDo { print() }
//                .andExpect {
//                    status { isCreated() }
//                    content {
//                        contentType(MediaType.APPLICATION_JSON)
//                    }
//                }
//
//            val userFromMongo: MongoUser? = userRepository.findByUserName(randomUser.userName!!).block()
//            userFromMongo shouldNotBe null
//            val actualUser = userFromMongo!!.toUser()
//
//            createdUserResponse.message shouldBe UserCreatedMessageResponseDto(CREATED_USER_MESSAGE).message
//
//            actualUser shouldBeEqualToComparingFields randomUser.toUser(userFromMongo.password).apply { id = null }
//        }
//
//        @Test
//        fun `should return 200 if user with email exist but and doesn't create one`() {
//            // given
//            val randomUserName = getRandomString()
//            // when
//            val performPost =
//                signUpUser(
//                    SignUpRequestDto(
//                        userName = randomUserName,
//                        email = newUser.email,
//                        password = newUser.password,
//                        roles = setOf(),
//                    ),
//                )
//
//            val createdUserResponse =
//                performPost.andReturn()
//                    .response
//                    .contentAsString
//                    .let { objectMapper.readValue(it, UserCreatedMessageResponseDto::class.java) }
//
//            performPost
//                .andDo { print() }
//                .andExpect { status { isCreated() } }
//                .andReturn()
//
//            // then
//            createdUserResponse.message shouldBe UserCreatedMessageResponseDto(CREATED_USER_MESSAGE).message
//            userRepository.existsByUserName(userName = randomUserName).block() shouldBe false
//        }
//
//        @Test
//        fun `should return 400 if user with userName exist`() {
//            // when
//            val performPost = signUpUser(newUser)
//            val result =
//                performPost
//                    .andDo { print() }
//                    .andExpect { status { isBadRequest() } }
//                    .andReturn()
//            // then
//            result.response.contentAsString shouldBe
//                UserAlreadyExistException.fromUserName(newUser.userName).message
//        }
//
//        private fun signUpUser(signUpUserRequest: SignUpRequestDto): ResultActionsDsl =
//            mockMvc.post(BASE_URL + SIGN_UP) {
//                contentType = MediaType.APPLICATION_JSON
//                content = objectMapper.writeValueAsString(signUpUserRequest)
//            }
//    }
    companion object {
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
