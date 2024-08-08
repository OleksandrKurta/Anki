package io.github.anki.anki.service

import io.github.anki.anki.repository.mongodb.UserRepository
import io.github.anki.anki.repository.mongodb.document.MongoUser
import io.github.anki.anki.service.model.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.stream.Collectors

@Service
class UserService @Autowired constructor(
    var authenticationManager: AuthenticationManager,
    var userRepository: UserRepository,
) : UserDetailsService {
    @Transactional
    @Throws(UsernameNotFoundException::class)
    override fun loadUserByUsername(username: String): UserDetails {
        val user: MongoUser? =
            userRepository.findByUsername(username)
                ?.orElseThrow {
                    UsernameNotFoundException(
                        "User Not Found with username: $username",
                    )
                }

        return User.build(user)
    }

    fun signUpByEmailPassword(user: User): User {
        val authentication: Authentication =
            authenticationManager
                .authenticate(
                    UsernamePasswordAuthenticationToken(
                        user.username,
                        user.password,
                    ),
                )

        SecurityContextHolder.getContext().authentication = authentication
        return authentication.principal as User
    }
}
