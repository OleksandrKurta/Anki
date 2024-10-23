package io.github.anki.anki.service.secure

import io.github.anki.anki.repository.mongodb.UserRepository
import io.github.anki.anki.repository.mongodb.document.MongoUser
import io.github.anki.anki.service.exceptions.UserDoesNotExistException
import io.github.anki.anki.service.model.mapper.toUser
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class UserDetailsService(
    private val userRepository: UserRepository,
) : ReactiveUserDetailsService {

    override fun findByUsername(userName: String): Mono<UserDetails> =
        userRepository.findByUserName(userName)
            .switchIfEmpty(Mono.error(UserDoesNotExistException.fromUserName(userName)))
            .map(MongoUser::toUser)
}
