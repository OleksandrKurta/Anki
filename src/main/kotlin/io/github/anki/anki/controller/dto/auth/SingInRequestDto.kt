package io.github.anki.anki.controller.dto.auth

import jakarta.validation.constraints.NotBlank

class SingInRequestDto(
    var username: @NotBlank String,
    var password: @NotBlank String,
)
