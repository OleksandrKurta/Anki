package io.github.anki.anki.secure.jwt

import io.github.anki.anki.service.model.User
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.UnsupportedJwtException
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import java.security.Key
import java.util.Date

@Component
class JwtUtils {
    @Value("\${anki.app.jwtSecret}")
    private val jwtSecret: String? = null

    @Value("\${anki.app.jwtExpirationMs}")
    private val jwtExpirationMs = 0

    fun generateJwtToken(authentication: Authentication): String {
        val userPrincipal: User = authentication.principal as User

        return Jwts.builder()
            .setSubject(userPrincipal.username)
            .setIssuedAt(Date())
            .setExpiration(Date(Date().time + jwtExpirationMs))
            .signWith(key(), SignatureAlgorithm.HS256)
            .compact()
    }

    private fun key(): Key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret))

    fun getUserNameFromJwtToken(token: String?): String =
        Jwts.parserBuilder().setSigningKey(
            key(),
        ).build().parseClaimsJws(token).body.subject

    fun validateJwtToken(authToken: String?): Boolean {
        try {
            Jwts.parserBuilder().setSigningKey(key()).build().parse(authToken)
            return true
        } catch (e: MalformedJwtException) {
            LOGGER.error("Invalid JWT token: {}", e.message)
        } catch (e: ExpiredJwtException) {
            LOGGER.error("JWT token is expired: {}", e.message)
        } catch (e: UnsupportedJwtException) {
            LOGGER.error("JWT token is unsupported: {}", e.message)
        } catch (e: IllegalArgumentException) {
            LOGGER.error("JWT claims string is empty: {}", e.message)
        }

        return false
    }

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(JwtUtils::class.java)
    }
}
