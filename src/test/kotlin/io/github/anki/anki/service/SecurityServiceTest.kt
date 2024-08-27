package io.github.anki.anki.service

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.anki.anki.controller.DecksController.Companion.BASE_URL
import io.github.anki.anki.controller.dto.NewDeckRequest
import io.github.anki.anki.controller.dto.auth.JwtResponseDto
import io.github.anki.anki.controller.dto.auth.SignInRequestDto
import io.github.anki.anki.controller.dto.auth.SignUpRequestDto
import io.github.anki.anki.controller.dto.mapper.toUser
import io.github.anki.anki.service.model.User
import io.github.anki.anki.service.model.mapper.toJwtDto
import io.github.anki.anki.service.secure.SecurityService
import io.github.anki.anki.service.secure.jwt.AuthTokenFilter.Companion.AUTH_HEADER_NAME
import io.github.anki.anki.service.secure.jwt.AuthTokenFilter.Companion.TOKEN_PREFIX
import io.github.anki.testing.MVCTest
import io.github.anki.testing.getRandomString
import io.github.anki.testing.randomUser
import io.github.anki.testing.testcontainers.TestContainersFactory
import io.github.anki.testing.testcontainers.with
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActionsDsl
import org.springframework.test.web.servlet.post
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Container
import kotlin.test.BeforeTest
import kotlin.test.Test

@MVCTest
class SecurityServiceTest @Autowired constructor(
    var userService: UserService,
    var securityService: SecurityService,
    var passwordEncoder: PasswordEncoder,
    val mockMvc: MockMvc,
    val objectMapper: ObjectMapper,
) {
    private lateinit var newUser: SignUpRequestDto
    private lateinit var encodedPassword: String
    private lateinit var expectedUser: User
    private lateinit var newDeckRequest: NewDeckRequest

    @BeforeTest
    fun setUp() {
        newDeckRequest =
            NewDeckRequest(
                name = getRandomString("initial"),
                description = getRandomString("initial"),
            )
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
    @DisplayName("SecurityService.authUser()")
    @TestInstance(Lifecycle.PER_CLASS)
    inner class RequestWithToken {

        @Test
        fun `should create valid jwt token if user exist`() {
            // given
            val signInRequestDto =
                SignInRequestDto(
                    userName = newUser.userName,
                    password = newUser.password,
                )
            val givenUser = userService.signUp(newUser.toUser(encodedPassword))
            // when
            val authentication = securityService.authUser(signInRequestDto)
            val token =
                authentication.let {
                    securityService.jwtUtils.generateJwtToken(it)
                }
            val response: JwtResponseDto =
                token.let {
                    userService.signIn(newUser.toUser(encodedPassword)).toJwtDto(it)
                }
            givenUser shouldNotBe null

            val performPost = postNewDeck(newDeckRequest, response.accessToken)
            // then
            performPost
                .andDo { print() }
                .andExpect {
                    status { isCreated() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                }
        }

        private fun postNewDeck(newDeck: NewDeckRequest, token: String): ResultActionsDsl =
            mockMvc.post(BASE_URL) {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(newDeck)
                headers {
                    header(AUTH_HEADER_NAME, TOKEN_PREFIX + token)
                }
            }
    }
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
