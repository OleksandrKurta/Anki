package io.github.anki.anki.service.secure.jwt

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

class AuthTokenFilter(
    private val jwtUtils: JwtUtils,
) : WebFilter {

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> =
        Mono.fromCallable {
                LOG.info("Start filtering the headers")
                jwtUtils.getAuthFromAuthHeader(exchange.request.headers)
        }
            .doOnNext { LOG.info("Got user from AUTH {}", it.user) }
            .flatMap { chain.filter(exchange).contextWrite(ReactiveSecurityContextHolder.withAuthentication(it)) }
            .doOnNext { LOG.info("Auth is set to context") }
            .doOnError { LOG.error("Cannot set user authentication") }
            .onErrorResume(
                IllegalArgumentException::class.java,
                { chain.filter(exchange) },
            )

    companion object {
        private val LOG: Logger = LoggerFactory.getLogger(AuthTokenFilter::class.java)
    }
}
