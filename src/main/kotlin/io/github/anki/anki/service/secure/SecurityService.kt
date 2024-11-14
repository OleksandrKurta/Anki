package io.github.anki.anki.service.secure

import io.github.anki.anki.service.model.User
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class SecurityService(
    private val authenticationManager: AuthenticationManager,
) {

    fun authUser(user: User): Mono<UserAuthentication> =
        authenticationManager.authenticate(user)

    fun getUserIdFromAuthentication(): Mono<String> =
        getCurrentAuthentication().flatMap { it.getUserId() }

    private fun getCurrentAuthentication(): Mono<UserAuthentication> =
        ReactiveSecurityContextHolder.getContext()
            .map { it.authentication }
            .cast(UserAuthentication::class.java)
}
