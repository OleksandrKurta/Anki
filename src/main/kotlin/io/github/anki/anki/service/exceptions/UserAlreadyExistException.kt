package io.github.anki.anki.service.exceptions

class UserAlreadyExistException(
    override val message: String = "User has already exist",
    override val cause: Throwable? = null,
) : BaseBadRequestException, RuntimeException(message, cause) {
    companion object {
        fun fromUserName(userName: String?) =
            UserAlreadyExistException("User has already exist with userName = $userName")
    }
}
