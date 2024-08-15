package io.github.anki.anki.controller


import io.github.anki.anki.controller.dto.auth.JwtResponse
import io.github.anki.anki.controller.dto.auth.LoginRequest
import io.github.anki.anki.controller.dto.auth.MessageResponse
import io.github.anki.anki.controller.dto.auth.SignupRequest
import io.github.anki.anki.repository.mongodb.RoleRepository
import io.github.anki.anki.repository.mongodb.UserRepository
import io.github.anki.anki.repository.mongodb.document.ERole
import io.github.anki.anki.repository.mongodb.document.MongoRole
import io.github.anki.anki.repository.mongodb.document.MongoUser
import io.github.anki.anki.secure.jwt.AuthEntryPointJwt
import io.github.anki.anki.secure.jwt.JwtUtils
import io.github.anki.anki.service.UserDetailsImpl
import jakarta.validation.Valid
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity

import org.springframework.web.bind.annotation.*
import java.util.function.Consumer
import java.util.stream.Collectors

import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder

@CrossOrigin(origins = ["*"], maxAge = 3600)
@RestController
@RequestMapping("/api/auth/v1")
class AuthController {
    @Autowired
    var authenticationManager: AuthenticationManager? = null

    @Autowired
    var userRepository: UserRepository? = null

    @Autowired
    var roleRepository: RoleRepository? = null

    @Autowired
    var encoder: PasswordEncoder? = null

    @Autowired
    var jwtUtils: JwtUtils? = null

    @PostMapping("/signin")
    fun authenticateUser(@RequestBody loginRequest: @Valid LoginRequest?): ResponseEntity<*> {
        val authentication: Authentication = authenticationManager!!.authenticate(
            UsernamePasswordAuthenticationToken(loginRequest!!.username, loginRequest.password)
        )

        SecurityContextHolder.getContext().setAuthentication(authentication)
        val jwt: String = jwtUtils!!.generateJwtToken(authentication)

        val userDetails: UserDetailsImpl = authentication.getPrincipal() as UserDetailsImpl
        val roles: List<String> = userDetails.getAuthorities().stream()
            .map { it -> it.getAuthority() }
            .collect(Collectors.toList())

        return ResponseEntity.ok<Any>(
            JwtResponse(
                jwt,
                userDetails.id,
                userDetails.getUsername(),
                userDetails.email,
                roles
            )
        )
    }

    @PostMapping("/signup")
    fun registerUser(@RequestBody signUpRequest: @Valid SignupRequest?): ResponseEntity<*> {
        if (userRepository!!.existsByUsername(signUpRequest!!.username) == true) {
            return ResponseEntity
                .badRequest()
                .body<Any>(MessageResponse("Error: Username is already taken!"))
        }

        if (userRepository!!.existsByEmail(signUpRequest.email) == true) {
            return ResponseEntity
                .badRequest()
                .body<Any>(MessageResponse("Error: Email is already in use!"))
        }

        // Create new user's account
        val user: MongoUser = MongoUser(
            signUpRequest.username,
            signUpRequest.email,
            encoder!!.encode(signUpRequest.password)
        )

        val strRoles: Set<String>? = signUpRequest.roles
        val roles: MutableSet<MongoRole?> = HashSet<MongoRole?>()

        if (strRoles == null) {
            val userRole: MongoRole? = roleRepository!!.findByName(ERole.ROLE_USER)
                ?.orElseThrow { RuntimeException("Error: Role is not found.") }
            roles.add(userRole)
        } else {
            strRoles.forEach(Consumer<String> { role: String? ->
                when (role) {
                    "admin" -> {
                        val adminRole: MongoRole? = roleRepository
                            ?.findByName(ERole.ROLE_ADMIN)
                            ?.orElseThrow { RuntimeException("Error: Role is not found.") }
                        roles.add(adminRole)
                    }
                    else -> {
                        val userRole: MongoRole? = roleRepository
                            ?.findByName(ERole.ROLE_USER)
                            ?.orElseThrow { RuntimeException("Error: Role is not found.") }
                        roles.add(userRole)
                    }
                }
            })
        }

        user.roles = roles
        userRepository!!.save(user)

        return ResponseEntity.ok<Any>(MessageResponse("User registered successfully!"))


    }
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(AuthEntryPointJwt::class.java)
    }
}