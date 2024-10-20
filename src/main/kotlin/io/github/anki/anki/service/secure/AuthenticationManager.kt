package io.github.anki.anki.service.secure

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
) : ReactiveAuthenticationManager {

    override fun authenticate(authentication: Authentication): Mono<Authentication> {
        val username: String = authentication.name
        val password: String = authentication.credentials.toString()

        return userDetailsService.findByUsername(username)
            .filter { userDetails: UserDetails -> passwordEncoder.matches(password, userDetails.password) }
            .map {
                UsernamePasswordAuthenticationToken(
                    it,
                    null,
                    it.authorities,
                )
            }
            .flatMap(this::setAuthentication)
            .map { it.authentication }
    }

    private fun setAuthentication(authentication: UsernamePasswordAuthenticationToken): Mono<SecurityContext> =
        ReactiveSecurityContextHolder
            .getContext()
            .doOnNext { it.authentication = authentication }
}
