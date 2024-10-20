package io.github.anki.anki.service.secure

import io.github.anki.anki.service.secure.jwt.AuthEntryPointJwt
import io.github.anki.anki.service.secure.jwt.AuthTokenFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository
import org.springframework.web.server.WebFilter

@Configuration
@EnableReactiveMethodSecurity
class WebSecurityConfig(
    private val unauthorizedHandler: AuthEntryPointJwt,
    private val authTokenFilter: WebFilter,
    private val authenticationManager: ReactiveAuthenticationManager
) {

    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain =
        http.csrf { csrf: ServerHttpSecurity.CsrfSpec -> csrf.disable() }
            .exceptionHandling { exception ->
                exception.authenticationEntryPoint(
                    unauthorizedHandler,
                )
            }
            .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
            .authenticationManager(authenticationManager)
            .authorizeExchange { auth ->
                auth
                    .pathMatchers("/api/auth/**")
                    .permitAll()
                    .anyExchange()
                    .authenticated()
            }
            .addFilterBefore(authTokenFilter, SecurityWebFiltersOrder.AUTHENTICATION)
            .build()
}
