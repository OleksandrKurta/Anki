package io.github.anki.anki.controller.dto.auth

import io.github.anki.anki.service.secure.TokenType

data class JwtResponseDto(
    val accessToken: String,
    val id: String?,
    val userName: String?,
    val email: String?,
    val roles: Set<String>,
) {
    val tokenType: TokenType = TokenType.BEARER
}
