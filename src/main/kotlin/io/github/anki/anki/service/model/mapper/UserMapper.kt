package io.github.anki.anki.service.model.mapper

import io.github.anki.anki.controller.dto.auth.JwtResponseDto
import io.github.anki.anki.repository.mongodb.document.MongoRole
import io.github.anki.anki.repository.mongodb.document.MongoUser
import io.github.anki.anki.repository.mongodb.document.Role
import io.github.anki.anki.service.exceptions.AuthoritiesNotFoundException
import io.github.anki.anki.service.model.User
import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.util.stream.Collectors

fun MongoUser.toUser(): User {
    return User(
        this.id.toString(),
        this.userName.toString(),
        this.email.toString(),
        this.password.toString(),
        this.roles.stream()
            .map { role ->
                SimpleGrantedAuthority(
                    role?.name
                        ?: throw AuthoritiesNotFoundException.fromUserName(this.userName),
                )
            }
            .collect(Collectors.toList()),
    )
}

fun User.toJwtDto(token: String): JwtResponseDto {
    val roles: Set<String> =
        this.authorities?.stream()
            ?.map { authority -> authority.toString() }
            ?.collect(Collectors.toSet())
            ?: throw AuthoritiesNotFoundException
                .fromUserName(this.userName)
    return JwtResponseDto(
        accessToken = token,
        id = this.id,
        email = this.email,
        userName = this.userName,
        roles = roles,
    )
}

fun User.toMongoUser(): MongoUser {
    val roles: Set<MongoRole> =
        this.authorities?.stream()
            ?.map { authority -> MongoRole(name = Role.valueOf(authority.toString()).name) }
            ?.collect(Collectors.toSet())
            ?: throw AuthoritiesNotFoundException
                .fromUserName(this.userName)
    return MongoUser(
        userName = this.username.toString(),
        email = this.email.toString(),
        password = this.password.toString(),
        roles = roles,
    )
}
