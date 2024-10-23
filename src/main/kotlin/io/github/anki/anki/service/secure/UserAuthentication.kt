package io.github.anki.anki.service.secure

import io.github.anki.anki.service.model.User
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import reactor.core.publisher.Mono

class UserAuthentication(val user: User, val creds: String, val roles: Collection<GrantedAuthority>) :
    UsernamePasswordAuthenticationToken(user, creds, roles) {

    fun getUserId(): Mono<String> = Mono.fromCallable { user.id }
}
