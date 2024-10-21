package io.github.anki.anki.service.secure.jwt

import io.github.anki.anki.service.exceptions.UserDoesNotExistException
import io.github.anki.anki.service.secure.AuthenticationManager
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.ReactiveSecurityContextHolder
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
) : WebFilter {

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> =
        Mono.fromCallable { jwtUtils.getAuthFromAuthHeader(exchange.request.headers) }
            .flatMap { chain.filter(exchange).contextWrite(ReactiveSecurityContextHolder.withAuthentication(it)) }
            .doOnError { LOG.error("Cannot set user authentication", it.stackTrace) }
            .onErrorResume(
                IllegalArgumentException::class.java,
                { Mono.empty() }
            )
            .then(chain.filter(exchange))
            .then()

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
