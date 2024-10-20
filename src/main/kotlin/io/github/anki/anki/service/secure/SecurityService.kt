package io.github.anki.anki.service.secure

import io.github.anki.anki.service.model.User
import io.github.anki.anki.service.secure.jwt.JwtUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class SecurityService @Autowired constructor(
    val authenticationManager: ReactiveAuthenticationManager,
    val jwtUtils: JwtUtils,
) {

    fun authUser(user: User): Mono<User> =
        authenticationManager
            .authenticate(UsernamePasswordAuthenticationToken(user.userName, user.password))
            .map { it.principal }
            .cast(User::class.java)

    // TODO: Rewirte other methods from jwtUtils
    fun getUserIdFromAuthHeader(header: HttpHeaders): String = jwtUtils.getUserIdFromAuthHeader(header)

}
