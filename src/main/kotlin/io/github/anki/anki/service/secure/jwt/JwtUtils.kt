package io.github.anki.anki.service.secure.jwt

import io.github.anki.anki.service.model.User
import io.github.anki.anki.service.secure.UserAuthentication
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import java.security.Key
import java.util.*

@Component
class JwtUtils {
    @Value("\${anki.app.jwtSecret}")
    private val JWT_SECRET: String? = null

    @Value("\${anki.app.jwtExpirationMs}")
    private val jwtExpirationMs = 0

    fun generateJwtToken(userDetails: UserDetails): String {
        val user: User = userDetails as User
        return Jwts.builder()
            .setClaims(
                mapOf(
                    "id" to user.id,
                    "email" to user.email,
                    "userName" to user.userName,
                    "roles" to user.authorities.map { it.authority }.joinToString(separator = ","),
                ),
            )
            .setIssuedAt(Date())
            .setExpiration(Date(Date().time + jwtExpirationMs))
            .signWith(getKey(), SignatureAlgorithm.HS256)
            .compact()
    }

    fun getAuthentication(token: String): UserAuthentication {
        LOG.info("Provided token {}", token)
        val claims: Claims =
            Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .body

        val authoritiesClaim = claims.get(AUTHORITIES_KEY)
        val authorities: Collection<GrantedAuthority> =
            if (authoritiesClaim == null) {
                AuthorityUtils.NO_AUTHORITIES
            } else {
                AuthorityUtils.commaSeparatedStringToAuthorityList(authoritiesClaim.toString())
            }

        return UserAuthentication(
            User(
                id = claims.get("id").toString(),
                userName = claims.get("userName").toString(),
                email = claims.get("email").toString(),
                password = null,
                authorities = authorities.toSet(),
            ),
            token,
            authorities,
        )
    }

    fun getAuthFromAuthHeader(header: HttpHeaders): UserAuthentication {
        val headerAuth: String? = header[AUTH_HEADER_NAME.lowercase()]?.get(0)
        val token: String =
            headerAuth?.substring(TOKEN_PREFIX.length, headerAuth.length)
                ?: throw IllegalArgumentException("Can't find token in headers")
        val isValidToken: Boolean = validateJwtToken(token)
        if (isValidToken) {
            return getAuthentication(token)
        }
        throw IllegalArgumentException("Provided token is not valid")
    }

    fun validateJwtToken(authToken: String?): Boolean {
        try {
            Jwts.parserBuilder().setSigningKey(getKey()).build().parse(authToken)
            LOG.info("Token is valid")
            return true
        } catch (e: IllegalArgumentException) {
            LOG.error("JWT claims string is empty: {}", e.message)
        }
        return false
    }

    private fun getKey(): Key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(JWT_SECRET))

    companion object {
        private val LOG: Logger = LoggerFactory.getLogger(JwtUtils::class.java)
        private const val AUTHORITIES_KEY: String = "roles"
        const val AUTH_HEADER_NAME: String = "Authorization"
        const val TOKEN_PREFIX: String = "Bearer "
    }
}
