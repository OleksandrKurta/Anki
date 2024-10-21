package io.github.anki.anki.service.model.mapper

import io.github.anki.anki.controller.dto.auth.JwtResponseDto
import io.github.anki.anki.repository.mongodb.document.MongoUser
import io.github.anki.anki.service.model.User
import io.github.anki.anki.service.secure.UserAuthentication
import org.springframework.security.core.authority.SimpleGrantedAuthority

fun MongoUser.toUser(): User {
    return User(
        this.id.toString(),
        this.userName,
        this.email,
        this.password,
        this.roles
            .map { role ->
                SimpleGrantedAuthority(
                    role
                        ?: throw IllegalArgumentException("User roles can not be null"),
                )
            },
    )
}

fun UserAuthentication.toJwtDto(): JwtResponseDto {
    val roles: Set<String> =
        this.authorities
            .map { authority -> authority.toString() }
            .toSet()
    require(roles.isEmpty(), { "User roles can not be null" })
    return JwtResponseDto(
        accessToken = this.creds,
        id = user.id,
        email = user.email,
        userName = user.userName,
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
