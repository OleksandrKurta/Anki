package io.github.anki.anki.controller.dto.mapper

import io.github.anki.anki.controller.dto.auth.JwtResponseDto
import io.github.anki.anki.controller.dto.auth.SingInRequestDto
import io.github.anki.anki.service.model.User
import org.springframework.security.core.context.SecurityContextHolder

fun SingInRequestDto.toUser(): User{
    return User(
        username = this.username,
        password = this.password,
    )
}

//fun User.toJwtDto(): JwtResponseDto {
//    return JwtResponseDto(
//        accessToken = jwtUtils.generateJwtToken(SecurityContextHolder.getContext().authentication),
//        id = this.id,
//        email = this.email,
//        username = this.username,
//        roles = this.roles
//    )
//}