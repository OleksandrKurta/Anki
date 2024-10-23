package io.github.anki.anki.service

import io.github.anki.anki.repository.mongodb.UserRepository
import io.github.anki.anki.repository.mongodb.document.MongoUser
import io.github.anki.anki.service.exceptions.UserAlreadyExistException
import io.github.anki.anki.service.model.User
import io.github.anki.anki.service.model.mapper.toMongoUser
import io.github.anki.anki.service.model.mapper.toUser
import io.github.anki.anki.service.secure.SecurityService
import io.github.anki.anki.service.secure.UserAuthentication
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DuplicateKeyException
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
                { mapDuplicateKeyException(it, user) },
            )

    private fun mapDuplicateKeyException(error: DuplicateKeyException, user: User): Mono<User> =
        Mono
            .just(error.stackTraceToString())
            .flatMap {
                when {
                    it.contains(MongoUser.USER_NAME)
                    -> Mono.error(UserAlreadyExistException.fromUserName(user.userName))

                    !it.contains(MongoUser.EMAIL) -> Mono.error(error)
                    else ->
                        Mono
                            .just(user)
                            .doFirst { LOG.error("User with this email {} already exists", user.email) }
                }
            }

    companion object {
        private val LOG: Logger = LoggerFactory.getLogger(UserService::class.java)
    }
}
