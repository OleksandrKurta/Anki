package io.github.anki.anki.service.model

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

data class User(
    var id: String? = null,
    val userName: String?,
    val email: String? = null,
    @field:JsonIgnore
    private val password: String?,
    private val authorities: Collection<GrantedAuthority> = emptySet(),
) : UserDetails {

    override fun getAuthorities(): Collection<GrantedAuthority> = authorities
    override fun getPassword(): String? = password
    override fun getUsername(): String? = userName
}
