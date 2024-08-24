package io.github.anki.anki.controller.dto.auth

import io.github.anki.anki.service.secure.TokenType

data class JwtResponseDto(
    var accessToken: String,
    var id: String?,
    var userName: String?,
    var email: String?,
    val roles: Set<String>,
) {
    var tokenType: TokenType = TokenType.BEARER
}
