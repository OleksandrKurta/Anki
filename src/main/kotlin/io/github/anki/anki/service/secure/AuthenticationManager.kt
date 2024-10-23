package io.github.anki.anki.service.secure

import io.github.anki.anki.service.model.User
import io.github.anki.anki.service.secure.jwt.JwtUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class AuthenticationManager(
    private val userDetailsService: ReactiveUserDetailsService,
    private val jwtUtils: JwtUtils,
) {

    fun authenticate(user: User): Mono<UserAuthentication> =
        userDetailsService.findByUsername(user.username)
            .doOnError { LOG.info(it.message) }
            .filter { user.password == it.password }
            .doOnNext { LOG.debug("Password is validated for user with userName {}", it.username) }
            .cast(User::class.java)
            .flatMap(this::getAuthentication)

    private fun getAuthentication(user: User): Mono<UserAuthentication> =
        Mono.fromCallable {
            UserAuthentication(
                user,
                jwtUtils.generateJwtToken(user),
            )
        }
            .doOnNext { LOG.info("UserAuthentication for user with userName {} is created", user.userName) }

    companion object {
        private val LOG: Logger = LoggerFactory.getLogger(AuthenticationManager::class.java)
    }
}
