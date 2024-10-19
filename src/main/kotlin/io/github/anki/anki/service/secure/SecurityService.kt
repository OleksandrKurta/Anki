package io.github.anki.anki.service.secure

import io.github.anki.anki.service.model.User
import io.github.anki.anki.service.secure.jwt.JwtUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class SecurityService @Autowired constructor(
    val authenticationManager: AuthenticationManager,
    val encoder: PasswordEncoder,
    val jwtUtils: JwtUtils,
) {

    fun authUser(user: User): User {
        val authentication: Authentication =
            authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(user.userName, user.password),
            )
        SecurityContextHolder.getContext().authentication = authentication
        return authentication.principal as User
    }

    fun getUserIdFromAuthHeader(header: HttpHeaders): String = jwtUtils.getUserIdFromAuthHeader(header)

}
