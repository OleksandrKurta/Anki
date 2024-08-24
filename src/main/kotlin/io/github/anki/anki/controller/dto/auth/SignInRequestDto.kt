package io.github.anki.anki.controller.dto.auth

import jakarta.validation.constraints.NotBlank

data class SignInRequestDto(
    var userName: @NotBlank String? = null,
    var password: @NotBlank String? = null,
) {
    override fun toString(): String = "${this.javaClass} with userName=$userName"
}
