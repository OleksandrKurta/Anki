package io.github.anki.anki.service.model

import com.fasterxml.jackson.annotation.JsonIgnore
import io.github.anki.anki.repository.mongodb.document.ERole
import io.github.anki.anki.repository.mongodb.document.MongoUser
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.util.stream.Collectors

class User(
    val id: String? = null,
    private val username: String?,
    val email: String? = null,
    @field:JsonIgnore
    private val password: String?,
    private val authorities: Collection<GrantedAuthority>? = null,
) : UserDetails {

    override fun getAuthorities(): Collection<GrantedAuthority>? = authorities
    override fun getPassword(): String? = password
    override fun getUsername(): String? = username
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val user = other as User
        return id == user.id
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result += email.hashCode()
        result += authorities.hashCode()
        return result
    }

    companion object {
        private const val serialVersionUID = 1L

        fun build(user: MongoUser?): User {
            val authorities: List<GrantedAuthority> =
                user!!.roles.stream()
                    .map { role -> SimpleGrantedAuthority(role!!.getName()!!.name) }
                    .collect(Collectors.toList())

            return User(
                user.id,
                user.username.toString(),
                user.email.toString(),
                user.password.toString(),
                authorities,
            )
        }
    }
}
