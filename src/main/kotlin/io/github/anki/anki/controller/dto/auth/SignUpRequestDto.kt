package io.github.anki.anki.controller.dto.auth

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

class SignUpRequestDto(
    var username:
    @NotBlank
    @Size(min = 3, max = 20)
    String,
    var email:
    @NotBlank
    @Size(max = 50)
    @Email
    String,
    var roles: Set<String>,
    var password:
    @NotBlank
    @Size(min = 6, max = 40)
    String,
)
