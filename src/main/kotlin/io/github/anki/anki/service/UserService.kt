package io.github.anki.anki.service

import io.github.anki.anki.repository.mongodb.UserRepository
import io.github.anki.anki.repository.mongodb.document.DocumentStatus
import io.github.anki.anki.repository.mongodb.document.MongoUser
import io.github.anki.anki.service.exceptions.UserAlreadyExistException
import io.github.anki.anki.service.exceptions.UserDoesNotExistException
import io.github.anki.anki.service.model.User
import io.github.anki.anki.service.model.mapper.toMongoUser
import io.github.anki.anki.service.model.mapper.toUser
import io.github.anki.anki.service.secure.SecurityService
import io.github.anki.anki.service.secure.UserAuthentication
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DuplicateKeyException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class UserService @Autowired constructor(
    private val userRepository: UserRepository,
    private val securityService: SecurityService,
) {

    fun signIn(user: User): Mono<UserAuthentication> =
        securityService.authUser(user)

    fun signUp(user: User): Mono<User> =
        userRepository.insert(user.toMongoUser())
            .map(MongoUser::toUser)
            .onErrorResume(
                DuplicateKeyException::class.java,
                { mapDuplicateKeyException(it, user) }
            )

    private fun mapDuplicateKeyException(error: DuplicateKeyException, user: User): Mono<User> {
        if (error.stackTraceToString().contains(MongoUser.USER_NAME)) {
            return Mono.error(UserAlreadyExistException.fromUserName(user.userName))
        } else if (!error.stackTraceToString().contains(MongoUser.EMAIL)) {
            return Mono.error(error)
        }
        return Mono.just(user)
    }

    companion object {
        private val LOG: Logger = LoggerFactory.getLogger(UserService::class.java)
    }
}
