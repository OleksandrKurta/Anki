package io.github.anki.anki.controller.dto.mapper

import io.github.anki.anki.controller.dto.auth.SignInRequestDto
import io.github.anki.anki.controller.dto.auth.SignUpRequestDto
import io.github.anki.anki.service.model.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.util.stream.Collectors

fun SignInRequestDto.toUser(): User {
    return User(
        userName = this.userName,
        password = this.password,
    )
}

fun SignUpRequestDto.toUser(password: String? = null): User {
    return User(
        email = this.email,
        userName = this.userName,
        password = password ?: this.password,
        authorities =
        this.roles.map
            { role -> SimpleGrantedAuthority(role) }.toSet()
    )
}
