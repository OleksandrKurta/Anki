package io.github.anki.anki.service.secure

import io.github.anki.anki.service.model.User
import io.github.anki.anki.service.secure.jwt.JwtUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.core.context.SecurityContext
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class SecurityService @Autowired constructor(
    private val authenticationManager: AuthenticationManager,
    private val jwtUtils: JwtUtils,
) {

    fun authUser(user: User): Mono<UserAuthentication> =
        authenticationManager.authenticate(user)

    fun getUserIdFromAuthentication(): Mono<String> =
        getCurrentAuthentication()
            .flatMap { it.getUserId() }

    private fun getCurrentAuthentication(): Mono<UserAuthentication> =
        ReactiveSecurityContextHolder
            .getContext()
            .map(SecurityContext::getAuthentication)
            .cast(UserAuthentication::class.java)


    // TODO: Rewirte other methods from jwtUtils
//    fun getUserIdFromAuthHeader(header: HttpHeaders): String = jwtUtils.getUserIdFromAuthHeader(header)
}
