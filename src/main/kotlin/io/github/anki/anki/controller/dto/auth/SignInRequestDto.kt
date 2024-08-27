package io.github.anki.anki.controller.dto.auth

import jakarta.validation.constraints.NotBlank

data class SignInRequestDto(
    val userName: @NotBlank String? = null,
    val password: @NotBlank String? = null,
) {
    override fun toString(): String = "${this.javaClass} with userName=$userName"
}
