package io.github.anki.anki.controller.dto.auth

import jakarta.validation.constraints.NotBlank


class LoginRequest {
    var username: @NotBlank String? = null

    var password: @NotBlank String? = null


}