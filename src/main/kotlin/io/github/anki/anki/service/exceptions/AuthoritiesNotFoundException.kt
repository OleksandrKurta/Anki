package io.github.anki.anki.service.exceptions

class AuthoritiesNotFoundException(
    override val message: String = "Authorities not found",
    override val cause: Throwable? = null,
) : RuntimeException(message, cause) {
    companion object {
        fun fromUserName(userName: String?) =
            UserAlreadyExistException("User with userName = $userName has no authorities")
    }
}
