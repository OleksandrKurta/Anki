package io.github.anki.anki.service.exceptions

class UserAlreadyExistException(
    override val message: String = "User already exists",
    override val cause: Throwable? = null,
) : BaseBadRequestException, RuntimeException(message, cause) {
    companion object {
        fun fromUserName(userName: String?) =
            UserAlreadyExistException("User already exists with userName = $userName")
    }
}
