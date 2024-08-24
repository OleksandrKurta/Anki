package io.github.anki.anki.controller

import io.github.anki.anki.controller.dto.auth.JwtResponseDto
import io.github.anki.anki.controller.dto.auth.MessageResponseDto
import io.github.anki.anki.controller.dto.auth.SignInRequestDto
import io.github.anki.anki.controller.dto.auth.SignUpRequestDto
import io.github.anki.anki.controller.dto.mapper.toUser
import io.github.anki.anki.service.UserService
import io.github.anki.anki.service.model.User
import io.github.anki.anki.service.model.mapper.toJwtDto
import io.github.anki.anki.service.secure.SecurityService
import jakarta.validation.Valid
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@CrossOrigin(
    origins = ["*"],
    maxAge = AuthController.MAX_AGE.toLong(),
)
@RestController
@RequestMapping(AuthController.BASE_URL)
class AuthController @Autowired constructor(
    val userService: UserService,
    val secureService: SecurityService,
) {

    @PostMapping(SIGN_IN)
    @ResponseStatus(HttpStatus.OK)
    fun authenticateUser(@RequestBody signInRequestDto: @Valid SignInRequestDto?): JwtResponseDto {
        LOG.info(
            "IN: ${Companion::class.java.name}: ${BASE_URL}${SIGN_IN} with userName${signInRequestDto!!.userName}",
        )

        userService.signIn(signInRequestDto.toUser())
        val authentication = secureService.authUser(signInRequestDto)
        val authUser = authentication.principal as User

        LOG.info("OUT: ${Companion::class.java.name}: ${BASE_URL}${SIGN_IN} with ${HttpStatus.OK}")
        return authUser.toJwtDto(secureService.jwtUtils.generateJwtToken(authentication))
    }

    @PostMapping(SIGN_UP)
    @ResponseStatus(HttpStatus.CREATED)
    fun registerUser(@RequestBody signUpRequestDto: @Valid SignUpRequestDto?): MessageResponseDto {
        LOG.info("IN: ${Companion::class.java.name}: ${BASE_URL}${SIGN_UP} with $signUpRequestDto")

        userService.signUp(signUpRequestDto!!.toUser(secureService.encoder.encode(signUpRequestDto.password)))

        LOG.info("OUT: ${Companion::class.java.name}: ${BASE_URL}${SIGN_IN} with ${HttpStatus.OK}")
        return MessageResponseDto(CREATED_USER_MESSAGE)
    }

    companion object {
        private val LOG: Logger = LoggerFactory.getLogger(AuthController::class.java)
        const val BASE_URL = "/api/auth/v1"
        const val SIGN_IN = "/signin"
        const val SIGN_UP = "/signup"
        const val MAX_AGE = 3600
        const val CREATED_USER_MESSAGE = "User registered successfully!"
    }
}
