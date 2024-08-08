package io.github.anki.anki.controller

import io.github.anki.anki.controller.dto.auth.JwtResponseDto
import io.github.anki.anki.controller.dto.auth.MessageResponseDto
import io.github.anki.anki.controller.dto.auth.SignUpRequestDto
import io.github.anki.anki.controller.dto.auth.SingInRequestDto
import io.github.anki.anki.controller.dto.mapper.toUser
import io.github.anki.anki.controller.exception.UnauthorizedException
import io.github.anki.anki.repository.mongodb.RoleRepository
import io.github.anki.anki.repository.mongodb.UserRepository
import io.github.anki.anki.repository.mongodb.document.ERole
import io.github.anki.anki.repository.mongodb.document.MongoRole
import io.github.anki.anki.repository.mongodb.document.MongoUser
import io.github.anki.anki.secure.jwt.AuthEntryPointJwt
import io.github.anki.anki.secure.jwt.JwtUtils
import io.github.anki.anki.service.UserService
import io.github.anki.anki.service.model.User
import jakarta.validation.Valid
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.function.Consumer
import java.util.stream.Collectors

@CrossOrigin(origins = ["*"], maxAge = 3600)
@RestController
@RequestMapping("/api/auth/v1")
class AuthController @Autowired constructor(
    var userRepository: UserRepository,
    val userService: UserService,
    var roleRepository: RoleRepository,
    var encoder: PasswordEncoder,
    var jwtUtils: JwtUtils,
) {

    @PostMapping("/signin")
    fun authenticateUser(@RequestBody singInRequestDto: @Valid SingInRequestDto?): ResponseEntity<*> {
        val userDetails: User = userService.signUpByEmailPassword(singInRequestDto!!.toUser())
        val roles: List<String> =
            userDetails.authorities!!.stream()
                .map { it.authority }
                .collect(Collectors.toList())
        LOGGER.info("User {${userDetails.id}} is logged in")
        return ResponseEntity.ok<Any>(
            JwtResponseDto(
                jwtUtils.generateJwtToken(SecurityContextHolder.getContext().authentication),
                userDetails.id,
                userDetails.username,
                userDetails.email,
                roles,
            ),
        )
    }

    @PostMapping("/signup")
    fun registerUser(@RequestBody signUpRequestDto: @Valid SignUpRequestDto?): ResponseEntity<*> {
        if (userRepository.existsByEmail(signUpRequestDto?.email) == true) {
            return ResponseEntity
                .badRequest()
                .body<Any>(UnauthorizedException("Error: Email is already in use!"))
        }
        val user: MongoUser =
            MongoUser(
                signUpRequestDto?.username,
                signUpRequestDto?.email,
                encoder.encode(signUpRequestDto?.password),
            )

        val strRoles: Set<String>? = signUpRequestDto?.roles
        val roles: MutableSet<MongoRole?> = HashSet<MongoRole?>()

        if (strRoles == null) {
            val userRole: MongoRole? =
                roleRepository.findByName(ERole.ROLE_USER)
                    ?.orElseThrow { UnauthorizedException("Error: Role is not found.") }
            roles.add(userRole)
        } else {
            strRoles.forEach(
                Consumer<String> { role: String? ->
                    when (role) {
                        "admin" -> {
                            val adminRole: MongoRole? =
                                roleRepository
                                    .findByName(ERole.ROLE_ADMIN)
                                    ?.orElseThrow { UnauthorizedException("Error: AdminRole is not found.") }
                            roles.add(adminRole)
                        }
                        "moderator" -> {
                            val modRole: MongoRole? =
                                roleRepository
                                    .findByName(ERole.ROLE_MODERATOR)
                                    ?.orElseThrow { UnauthorizedException("Error: ModeratorRole is not found.") }
                            roles.add(modRole)
                        }
                        else -> {
                            val userRole: MongoRole? =
                                roleRepository
                                    .findByName(ERole.ROLE_USER)
                                    ?.orElseThrow { UnauthorizedException("Error: Any suitable role was not found!") }
                            LOGGER.error("Error: trying to log in with non-exist role")
                            roles.add(userRole)
                        }
                    }
                },
            )
        }

        user.roles = roles
        userRepository.save(user)

        return ResponseEntity.ok<Any>(MessageResponseDto("User registered successfully!"))
    }
    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(AuthEntryPointJwt::class.java)
    }
}
