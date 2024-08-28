package io.github.anki.anki.service.secure.jwt

import io.github.anki.anki.service.UserService
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.util.StringUtils
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException

class AuthTokenFilter : OncePerRequestFilter() {
    @Autowired
    private val jwtUtils: JwtUtils? = null

    @Autowired
    private val userDetailsService: UserService? = null

    @Throws(ServletException::class, IOException::class)
    @Suppress("Detekt.TooGenericExceptionCaught")
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        try {
            val jwt = parseJwt(request)
            if (jwt != null && jwtUtils?.validateJwtToken(jwt)
                ?: throw IllegalArgumentException("JwtUtils not initialized")
            ) {
                val userName: String = jwtUtils.getUserNameFromJwtToken(jwt)

                val userDetails: UserDetails = userDetailsService!!.loadUserByUsername(userName)
                val authentication =
                    UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.authorities,
                    )
                authentication.details = WebAuthenticationDetailsSource().buildDetails(request)

                SecurityContextHolder.getContext().authentication = authentication
            }
        } catch (e: Exception) {
            LOG.error("Cannot set user authentication: {}", e)
        }

        filterChain.doFilter(request, response)
    }

    private fun parseJwt(request: HttpServletRequest): String? {
        val headerAuth = request.getHeader(AUTH_HEADER_NAME)
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith(TOKEN_PREFIX)) {
            return headerAuth.substring(TOKEN_PREFIX.length, headerAuth.length)
        }

        return null
    }

    companion object {
        private val LOG: Logger = LoggerFactory.getLogger(AuthTokenFilter::class.java)
        const val AUTH_HEADER_NAME: String = "Authorization"
        const val TOKEN_PREFIX: String = "Bearer "
    }
}
