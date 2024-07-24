package io.github.anki.anki.service

import com.fasterxml.jackson.annotation.JsonIgnore
import io.github.anki.anki.repository.mongodb.document.MongoUser
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.util.stream.Collectors


class UserDetailsImpl(
    val id: String?,
    private val username: String?,
    val email: String?,
    @field:JsonIgnore private val password: String,
    private val authorities: Collection<GrantedAuthority>
) : UserDetails {
    override fun getAuthorities(): Collection<GrantedAuthority> {
        return authorities
    }

    override fun getPassword(): String {
        return password
    }

    override fun getUsername(): String? {
        return username
    }

    override fun isAccountNonExpired(): Boolean {
        return true
    }

    override fun isAccountNonLocked(): Boolean {
        return true
    }

    override fun isCredentialsNonExpired(): Boolean {
        return true
    }

    override fun isEnabled(): Boolean {
        return true
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val user = o as UserDetailsImpl
        return id == user.id
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + username.hashCode()
        result = 31 * result + email.hashCode()
        result = 31 * result + password.hashCode()
        result = 31 * result + authorities.hashCode()
        return result
    }

    companion object {
        private const val serialVersionUID = 1L

        fun build(user: MongoUser?): UserDetailsImpl {
            val authorities: List<GrantedAuthority> = user!!.roles.stream()
                .map { role -> SimpleGrantedAuthority(role!!.getName()!!.name) }
                .collect(Collectors.toList())

            return UserDetailsImpl(
                user.id,
                user.username.toString(),
                user.email.toString(),
                user.password.toString(),
                authorities
            )
        }
    }
}