package io.github.anki.anki.service.secure

import io.github.anki.anki.controller.dto.auth.SignInRequestDto
import io.github.anki.anki.service.secure.jwt.JwtUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class SecurityService @Autowired constructor(
    val authenticationManager: AuthenticationManager,
    var encoder: PasswordEncoder,
    var jwtUtils: JwtUtils,
) {

    fun authUser(user: SignInRequestDto): Authentication {
        val authentication: Authentication =
            authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(user.userName, user.password),
            )
        SecurityContextHolder.getContext().setAuthentication(authentication)
        return authentication
    }
}
