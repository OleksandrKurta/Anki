package io.github.anki.anki.controller.dto.auth

data class JwtResponseDto(
    val accessToken: String,
    val id: String?,
    val userName: String?,
    val email: String?,
    val roles: Set<String>,
)
