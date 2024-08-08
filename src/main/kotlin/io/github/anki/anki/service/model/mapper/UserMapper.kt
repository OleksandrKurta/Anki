package io.github.anki.anki.service.model.mapper

import io.github.anki.anki.repository.mongodb.document.ERole
import io.github.anki.anki.repository.mongodb.document.MongoDeck
import io.github.anki.anki.repository.mongodb.document.MongoRole
import io.github.anki.anki.repository.mongodb.document.MongoUser
import io.github.anki.anki.service.model.User
import org.bson.types.ObjectId
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.util.stream.Collectors

fun MongoUser.toUser(): User
    {
    return User(
        this.id,
        this.username.toString(),
        this.email.toString(),
        this.password.toString(),
        this.roles.stream()
                    .map { role -> SimpleGrantedAuthority(role!!.getName()!!.name) }
                    .collect(Collectors.toList()),
    )
}

fun User.toMongoUser(): MongoUser {
    val roles: Set<MongoRole> = this.authorities!!.stream()
        .map { authority -> MongoRole(ERole.valueOf(authority.toString()))}
        .collect(Collectors.toSet())
    return MongoUser(
        id = this.id,
        username = this.username.toString(),
        email = this.email.toString(),
        password = this.password.toString(),
        roles = roles,
    )

}

