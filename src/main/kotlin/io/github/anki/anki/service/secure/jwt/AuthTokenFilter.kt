package io.github.anki.anki.service.secure.jwt

import io.github.anki.anki.service.exceptions.UserDoesNotExistException
import io.github.anki.anki.service.secure.AuthenticationManager
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

@Component
class AuthTokenFilter(
    private val jwtUtils: JwtUtils,
    private val authenticationManager: AuthenticationManager,
) : WebFilter {

    @Autowired
    @Suppress("LateinitUsage")
    private lateinit var userDetailsService: ReactiveUserDetailsService

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        if (shouldFilter(exchange).block() == true) {
            return chain.filter(exchange)
        }

        return Mono.fromCallable { parseJwt(exchange.request) }
            .filter(jwtUtils::validateJwtToken)
            .flatMap(this::getUserByProvidedJwt)
            .map { UsernamePasswordAuthenticationToken(it, null, it.authorities) }
            .flatMap(authenticationManager::setAuthentication)
            .doOnError { LOG.error("Cannot set user authentication", it.stackTrace) }
            .onErrorResume {
                Mono.empty()
            }
            .then(chain.filter(exchange))
    }

    private fun getUserByProvidedJwt(jwtToken: String): Mono<UserDetails> {
        val userName: String = jwtUtils.getUserNameFromJwtToken(jwtToken)
        return userDetailsService
            .findByUsername(userName)
            .switchIfEmpty(Mono.error(UserDoesNotExistException.fromUserName(userName)))
    }

    private fun parseJwt(request: ServerHttpRequest): String {
        val headerAuth = request.headers.getFirst(AUTH_HEADER_NAME)
        headerAuth ?: throw IllegalArgumentException("Authorization header is not provided")
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith(TOKEN_PREFIX)) {
            return headerAuth.substring(TOKEN_PREFIX.length, headerAuth.length)
        }
        throw IllegalArgumentException("Can not parse provided jwt token")
    }

    // TODO: rewrite with ALLOWED_ENDPOINTS
    private fun shouldFilter(exchange: ServerWebExchange): Mono<Boolean> =
        Mono
            .just(exchange.request.path.pathWithinApplication().value().startsWith("/api/auth/v1/"))

    companion object {
        private val LOG: Logger = LoggerFactory.getLogger(AuthTokenFilter::class.java)
        private val ALLOWED_ENDPOINTS: List<String> = listOf("/api/auth/v1/")
        const val AUTH_HEADER_NAME: String = "Authorization"
        const val TOKEN_PREFIX: String = "Bearer "
    }
}
