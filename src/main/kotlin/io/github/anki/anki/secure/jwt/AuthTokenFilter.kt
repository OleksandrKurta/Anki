package io.github.anki.anki.secure.jwt

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
    private val userService: UserService? = null

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        try {
            val jwt = parseJwt(request)
            if (jwt != null && jwtUtils!!.validateJwtToken(jwt)) {
                val username: String = jwtUtils.getUserNameFromJwtToken(jwt)

                val userDetails: UserDetails = userService!!.loadUserByUsername(username)
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
            Companion.LOGGER.error("Cannot set user authentication: ", e)
        }

        filterChain.doFilter(request, response)
    }

    private fun parseJwt(request: HttpServletRequest): String? {
        val headerAuth = request.getHeader("Authorization")
        val tokenPrefix = "Bearer "
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith(tokenPrefix)) {
            return headerAuth.substring(tokenPrefix.length, headerAuth.length)
        }

        return null
    }

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(AuthTokenFilter::class.java)
    }
}
