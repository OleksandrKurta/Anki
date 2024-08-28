package io.github.anki.anki.service.secure.jwt

import io.github.anki.anki.service.model.User
import io.github.anki.anki.service.secure.jwt.AuthTokenFilter.Companion.AUTH_HEADER_NAME
import io.github.anki.anki.service.secure.jwt.AuthTokenFilter.Companion.TOKEN_PREFIX
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
import org.springframework.http.HttpHeaders
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import java.security.Key
import java.util.*

@Component
class JwtUtils {
    @Value("\${anki.app.jwtSecret}")
    private val jwtSecret: String? = null

    @Value("\${anki.app.jwtExpirationMs}")
    private val jwtExpirationMs = 0

    fun generateJwtToken(authentication: Authentication): String {
        val userPrincipal: User = authentication.principal as User

        return Jwts.builder()
            .setClaims(
                mapOf(
                    "email" to userPrincipal.email,
                    "userName" to userPrincipal.userName,
                    "id" to userPrincipal.id,
                ),
            )
            .setSubject("${userPrincipal.getUsername()}")
            .setIssuedAt(Date())
            .setExpiration(Date(Date().time + jwtExpirationMs))
            .signWith(key(), SignatureAlgorithm.HS256)
            .compact()
    }

    private fun key(): Key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret))

    fun getUserNameFromJwtToken(token: String?): String {
        return Jwts.parserBuilder().setSigningKey(key()).build()
            .parseClaimsJws(token)
            .body
            .subject
    }

    fun getUserIdFromJwtToken(token: String?): String =
        Jwts.parserBuilder()
            .setSigningKey(key())
            .build()
            .parseClaimsJws(token)
            .body["id"].toString()

    fun getUserIdFromAuthHeader(header: HttpHeaders): String {
        val headerAuth = header[AUTH_HEADER_NAME.lowercase()]?.get(0)
        val token = headerAuth?.substring(TOKEN_PREFIX.length, headerAuth.length)
        return getUserIdFromJwtToken(token)
    }

    fun validateJwtToken(authToken: String?): Boolean {
        try {
            Jwts.parserBuilder().setSigningKey(key()).build().parse(authToken)
            return true
        } catch (e: IllegalArgumentException) {
            LOG.error("JWT claims string is empty: {}", e.message)
        }

        return false
    }

    companion object {
        private val LOG: Logger = LoggerFactory.getLogger(JwtUtils::class.java)
    }
}
