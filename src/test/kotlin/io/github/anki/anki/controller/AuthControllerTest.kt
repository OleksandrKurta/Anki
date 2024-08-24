package io.github.anki.anki.controller

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.anki.anki.controller.AuthController.Companion.BASE_URL
import io.github.anki.anki.controller.AuthController.Companion.CREATED_USER_MESSAGE
import io.github.anki.anki.controller.AuthController.Companion.SIGN_IN
import io.github.anki.anki.controller.AuthController.Companion.SIGN_UP
import io.github.anki.anki.controller.dto.auth.JwtResponseDto
import io.github.anki.anki.controller.dto.auth.MessageResponseDto
import io.github.anki.anki.controller.dto.auth.SignInRequestDto
import io.github.anki.anki.controller.dto.auth.SignUpRequestDto
import io.github.anki.anki.controller.dto.mapper.toUser
import io.github.anki.anki.repository.mongodb.UserRepository
import io.github.anki.anki.service.exceptions.UserAlreadyExistException
import io.github.anki.anki.service.exceptions.UserDoesNotExistException
import io.github.anki.anki.service.model.mapper.toJwtDto
import io.github.anki.anki.service.model.mapper.toMongoUser
import io.github.anki.anki.service.model.mapper.toUser
import io.github.anki.anki.service.secure.jwt.JwtUtils
import io.github.anki.testing.MVCTest
import io.github.anki.testing.getRandomString
import io.github.anki.testing.randomUser
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
import org.springframework.http.MediaType
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActionsDsl
import org.springframework.test.web.servlet.post
import kotlin.test.BeforeTest

@MVCTest
class AuthControllerTest @Autowired constructor(
    val mockMvc: MockMvc,
    val objectMapper: ObjectMapper,
    val userRepository: UserRepository,
    val jwtUtil: JwtUtils,
    var encoder: PasswordEncoder,
    val authenticationManager: AuthenticationManager,
) {

    private lateinit var newUser: SignUpRequestDto
    private lateinit var token: String

    @BeforeTest
    fun setUp() {
        newUser = SignUpRequestDto.randomUser()
        val user = newUser.toUser(encoder.encode(newUser.password))
        userRepository.insert(user.toMongoUser()).id.toString()
        val authentication: Authentication =
            authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(user.userName, newUser.password),
            )
        SecurityContextHolder.getContext().setAuthentication(authentication)
        token = jwtUtil.generateJwtToken(authentication)
    }

    @Nested
    @DisplayName("POST $BASE_URL$SIGN_IN")
    @TestInstance(Lifecycle.PER_CLASS)
    inner class PostSignInUser {

        // positive user not exist username or pass is wrong
        @Test
        fun `should create new User always`() {
            // when
            val performPost = signInUser(SignInRequestDto(newUser.userName, newUser.password))

            val response =
                performPost.andReturn()
                    .response
                    .contentAsString
                    .let { objectMapper.readValue(it, JwtResponseDto::class.java) }

            // then
            performPost
                .andDo { print() }
                .andExpect {
                    status { isOk() }
                    content {
                        contentType(MediaType.APPLICATION_JSON)
                        json(objectMapper.writeValueAsString(response))
                    }
                }

            val userFromMongo = userRepository.findById(ObjectId(response.id))

            userFromMongo shouldNotBe null

            response shouldBe userFromMongo!!.toUser().toJwtDto(token)
        }

        @Test
        fun `should return 400 if user does not exist`() {
            // given
            val randomUserName = getRandomString()

            // when
            val performPost = signInUser(SignInRequestDto(randomUserName, newUser.password))
            val result =
                performPost
                    .andDo { print() }
                    .andExpect { status { isBadRequest() } }
                    .andReturn()
            // then
            result.response.contentAsString shouldBe
                UserDoesNotExistException.fromUserName(randomUserName).message
        }

        private fun signInUser(signInUserRequest: SignInRequestDto): ResultActionsDsl =
            mockMvc.post(BASE_URL + SIGN_IN) {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(signInUserRequest)
            }
    }

    @Nested
    @DisplayName("POST $BASE_URL$SIGN_UP")
    @TestInstance(Lifecycle.PER_CLASS)
    inner class PostSignUpUser {

        // positive user exist with email, username
        @Test
        fun `should sign up User always`() {
            // when
            val randomUser = SignUpRequestDto.randomUser()
            val performPost = signUpUser(randomUser)

            val createdUserResponse =
                performPost.andReturn()
                    .response
                    .contentAsString
                    .let { objectMapper.readValue(it, MessageResponseDto::class.java) }

            // then
            performPost
                .andDo { print() }
                .andExpect {
                    status { isCreated() }
                    content {
                        contentType(MediaType.APPLICATION_JSON)
                    }
                }

            val userFromMongo = randomUser.userName?.let { userRepository.findByUserName(it) }
            userFromMongo shouldNotBe null
            val actualUser = userFromMongo!!.toUser()
            actualUser.id = null

            createdUserResponse.message shouldBe MessageResponseDto(CREATED_USER_MESSAGE).message

            actualUser shouldBeEqualToComparingFields randomUser.toUser(userFromMongo.password)
        }

        @Test
        fun `should return 200 if user with email exist but and doesn't create one`() {
            // given
            newUser.userName = getRandomString()
            // when
            val performPost = signUpUser(newUser)

            val createdUserResponse =
                performPost.andReturn()
                    .response
                    .contentAsString
                    .let { objectMapper.readValue(it, MessageResponseDto::class.java) }

            performPost
                .andDo { print() }
                .andExpect { status { isCreated() } }
                .andReturn()

            // then
            createdUserResponse.message shouldBe MessageResponseDto(CREATED_USER_MESSAGE).message
            userRepository.existsByUserName(userName = newUser.userName) shouldBe false
        }

        @Test
        fun `should return 400 if user with userName exist`() {
            // when
            val performPost = signUpUser(newUser)
            val result =
                performPost
                    .andDo { print() }
                    .andExpect { status { isBadRequest() } }
                    .andReturn()
            // then
            result.response.contentAsString shouldBe
                UserAlreadyExistException.fromUserName(newUser.userName).message
        }

        private fun signUpUser(signUpUserRequest: SignUpRequestDto): ResultActionsDsl =
            mockMvc.post(BASE_URL + SIGN_UP) {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(signUpUserRequest)
            }
    }
}
