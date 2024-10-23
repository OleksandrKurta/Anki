package io.github.anki.anki.service.secure

import io.github.anki.anki.service.model.User
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import reactor.core.publisher.Mono

class UserAuthentication(val user: User, val creds: String) :
    UsernamePasswordAuthenticationToken(user, creds, user.authorities) {

    fun getUserId(): Mono<String> = Mono.fromCallable { user.id }
}
