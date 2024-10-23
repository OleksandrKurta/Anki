package io.github.anki.anki.service.secure.jwt

import org.springframework.http.HttpStatus
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.server.ServerAuthenticationEntryPoint
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
class AuthEntryPointJwt : ServerAuthenticationEntryPoint {

    override fun commence(exchange: ServerWebExchange, ex: AuthenticationException): Mono<Void> {
        val response: ServerHttpResponse = exchange.response
        response.setStatusCode(HttpStatus.UNAUTHORIZED)
        return response.writeWith(
            Mono.just(response.bufferFactory().wrap("Error: Unauthorized1".encodeToByteArray())),
        )
    }
}
