package io.github.anki.anki.service.secure

import io.github.anki.anki.controller.AuthController
import io.github.anki.anki.service.model.User
import io.github.anki.anki.service.secure.jwt.JwtUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class AuthenticationManager(
    private val userDetailsService: ReactiveUserDetailsService,
    private val passwordEncoder: PasswordEncoder,
    private val jwtUtils: JwtUtils,
) {

    fun authenticate(user: User): Mono<UserAuthentication> =
        userDetailsService.findByUsername(user.username)
            .filter { passwordEncoder.matches(user.password.toString(), it.password) }
            .cast(User::class.java)
            .map {
                UserAuthentication(
                    it,
                    jwtUtils.generateJwtToken(it),
                    it.authorities,
                )
            }

    companion object {
        private val LOG: Logger = LoggerFactory.getLogger(AuthenticationManager::class.java)
    }
}
