package io.github.anki.anki.controller.dto.auth


class JwtResponse(
    var accessToken: String,
    var id: String?,
    var username: String?,
    var email: String?,
    val roles: List<String>
) {
    var tokenType: String = "Bearer"
}