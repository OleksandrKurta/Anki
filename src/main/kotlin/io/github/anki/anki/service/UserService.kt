package io.github.anki.anki.service

import io.github.anki.anki.repository.mongodb.UserRepository
import io.github.anki.anki.repository.mongodb.document.MongoUser
import io.github.anki.anki.service.exceptions.UserAlreadyExistException
import io.github.anki.anki.service.exceptions.UserDoesNotExistException
import io.github.anki.anki.service.model.User
import io.github.anki.anki.service.model.mapper.toMongoUser
import io.github.anki.anki.service.model.mapper.toUser
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DuplicateKeyException
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class UserService : UserDetailsService {
    @Autowired
    var userRepository: UserRepository? = null

    @Throws(UsernameNotFoundException::class)
    override fun loadUserByUsername(userName: String): UserDetails {
        val mongoUser: MongoUser =
            userRepository?.findByUserName(userName)
                ?: throw UsernameNotFoundException("User Not Found with username: $userName")
        return mongoUser.toUser()
    }

    fun signIn(user: User): User {
        if (userRepository?.existsByUserName(user.userName)!!) {
            return user
        }
        throw UserDoesNotExistException.fromUserName(user.userName)
    }

    fun signUp(user: User): User? {
        try {
            return userRepository?.insert(user.toMongoUser())?.toUser()
        } catch (ex: DuplicateKeyException) {
            LOG.error(ex.toString())
            if (ex.stackTraceToString().contains(MongoUser.USER_NAME)) {
                throw UserAlreadyExistException.fromUserName(user.userName)
            } else if (!ex.stackTraceToString().contains(MongoUser.EMAIL)) {
                throw ex
            }
        }
        return null
    }

    companion object {
        private val LOG: Logger = LoggerFactory.getLogger(UserService::class.java)
    }
}
