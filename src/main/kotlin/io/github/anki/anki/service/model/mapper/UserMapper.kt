package io.github.anki.anki.service.model.mapper

import io.github.anki.anki.controller.dto.auth.JwtResponseDto
import io.github.anki.anki.repository.mongodb.document.MongoRole
import io.github.anki.anki.repository.mongodb.document.MongoUser
import io.github.anki.anki.service.model.User
import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.util.stream.Collectors

fun MongoUser.toUser(): User {
    return User(
        this.id.toString(),
        this.userName.toString(),
        this.email.toString(),
        this.password.toString(),
        this.roles
            .map { role ->
                SimpleGrantedAuthority(
                    role
                        ?: throw IllegalArgumentException("User roles can not be null"),
                )
            },
    )
}

fun User.toJwtDto(token: String): JwtResponseDto {
    val roles: Set<String> =
        this.authorities
            ?.map { authority -> authority.toString() }
            ?.toSet()
            ?: throw IllegalArgumentException("User roles can not be null")
    return JwtResponseDto(
        accessToken = token,
        id = this.id,
        email = this.email,
        userName = this.userName,
        roles = roles,
    )
}

fun User.toMongoUser(): MongoUser {
    val roles: Set<String> =
        this.authorities
            ?.map { it.toString() }
            ?.toSet()
            ?: throw IllegalArgumentException("User authorities can not be null")
    return MongoUser(
        userName = this.username.toString(),
        email = this.email.toString(),
        password = this.password.toString(),
        roles = roles,
    )
}
