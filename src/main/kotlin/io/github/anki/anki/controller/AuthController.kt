package io.github.anki.anki.controller

import io.github.anki.anki.controller.dto.auth.JwtResponseDto
import io.github.anki.anki.controller.dto.auth.SignInRequestDto
import io.github.anki.anki.controller.dto.auth.SignUpRequestDto
import io.github.anki.anki.controller.dto.auth.UserCreatedMessageResponseDto
import io.github.anki.anki.controller.dto.mapper.toUser
import io.github.anki.anki.service.UserService
import io.github.anki.anki.service.model.mapper.toJwtDto
import io.github.anki.anki.service.secure.SecurityService
import io.github.anki.anki.service.secure.jwt.JwtUtils
import jakarta.validation.Valid
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@CrossOrigin(
    origins = ["*"],
    maxAge = AuthController.MAX_AGE.toLong(),
)
@RestController
@RequestMapping(AuthController.BASE_URL)
class AuthController @Autowired constructor(
    val userService: UserService,
    val secureService: SecurityService,
    val jwtUtils: JwtUtils,
    val encoder: PasswordEncoder,
) {

    @PostMapping(SIGN_IN, produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.OK)
    fun authenticateUser(@RequestBody signInRequestDto: @Valid SignInRequestDto): Mono<JwtResponseDto> =
        userService
            .signIn(signInRequestDto.toUser())
            .flatMap { secureService.authUser(it) }
            .doFirst {
                LOG.info(
                    "IN: ${AuthController::class.java.name}:" +
                        " ${BASE_URL}${SIGN_IN} with userName${signInRequestDto.userName}",
                )
            }
            .map { user -> user.toJwtDto(jwtUtils.generateJwtToken(user)) }
            .doOnNext {
                LOG.info("OUT: ${AuthController::class.java.name}: $BASE_URL$SIGN_IN with ${HttpStatus.OK}")
            }

    @PostMapping(SIGN_UP)
    @ResponseStatus(HttpStatus.CREATED)
    fun registerUser(@RequestBody signUpRequestDto: @Valid SignUpRequestDto): Mono<UserCreatedMessageResponseDto> =
        userService
            .signUp(signUpRequestDto.toUser(encoder.encode(signUpRequestDto.password)))
            .doFirst {LOG.info("IN: ${AuthController::class.java.name}: $BASE_URL$SIGN_UP with $signUpRequestDto")}
            .then(Mono.just(UserCreatedMessageResponseDto(CREATED_USER_MESSAGE)))
            .doOnNext { LOG.info("OUT: ${AuthController::class.java.name}: $BASE_URL$SIGN_IN with ${HttpStatus.OK}") }

    companion object {
        private val LOG: Logger = LoggerFactory.getLogger(AuthController::class.java)
        const val BASE_URL = "/api/auth/v1"
        const val SIGN_IN = "/signin"
        const val SIGN_UP = "/signup"
        const val MAX_AGE = 3600
        const val CREATED_USER_MESSAGE = "User registered successfully!"
    }
}
