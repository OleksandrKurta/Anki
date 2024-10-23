package io.github.anki.anki.controller.dto.mapper

import io.github.anki.anki.controller.dto.auth.SignInRequestDto
import io.github.anki.anki.controller.dto.auth.SignUpRequestDto
import io.github.anki.anki.service.model.User
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.crypto.password.PasswordEncoder

fun SignInRequestDto.toUser(): User =
    User(
        userName = this.userName,
        password = this.password,
    )

fun SignUpRequestDto.toUser(encoder: PasswordEncoder): User =
    User(
        email = this.email,
        userName = this.userName,
        password = encoder.encode(this.password),
        authorities =
        this.roles.map
            { role -> SimpleGrantedAuthority(role) }.toSet(),
    )
