package io.github.anki.anki.controller.dto.auth

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

class SignUpRequestDto(
    val userName:
    @NotBlank
    @Size(min = 3, max = 20)
    String? = null,
    val email:
    @NotBlank
    @Size(max = 50)
    @Email
    String? = null,
    var roles: Set<String>? = null,
    val password:
    @NotBlank
    @Size(min = 6, max = 40)
    String? = null,
) {

    fun setRole(roles: Set<String>?) {
        this.roles = roles
    }

    companion object
}
