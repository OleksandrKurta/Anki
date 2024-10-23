package io.github.anki.anki.service.exceptions

class UserDoesNotExistException(
    override val message: String = "User does not exist",
    override val cause: Throwable? = null,
) : BaseBadRequestException, RuntimeException(message, cause) {
    companion object {
        fun fromUserName(userName: String?) =
            UserDoesNotExistException("User with userName = $userName does not exist")
    }
}
