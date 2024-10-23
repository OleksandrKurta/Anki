package io.github.anki.anki.service.secure

import io.github.anki.anki.service.secure.jwt.AuthEntryPointJwt
import io.github.anki.anki.service.secure.jwt.AuthTokenFilter
import io.github.anki.anki.service.secure.jwt.JwtUtils
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository
import org.springframework.web.server.WebFilter

@Configuration
@EnableWebFluxSecurity
class WebSecurityConfig(
    private val unauthorizedHandler: AuthEntryPointJwt,
    private val jwtUtils: JwtUtils,
) {

    private fun getWebFilter(): WebFilter = AuthTokenFilter(jwtUtils)

    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain =
        http.csrf { it.disable() }
            .exceptionHandling { it.authenticationEntryPoint(unauthorizedHandler) }
            .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
            .authorizeExchange {
                it
                    .pathMatchers("/api/auth/**")
                    .permitAll()
                    .anyExchange()
                    .authenticated()
            }
            .addFilterBefore(getWebFilter(), SecurityWebFiltersOrder.AUTHENTICATION)
            .build()
}
