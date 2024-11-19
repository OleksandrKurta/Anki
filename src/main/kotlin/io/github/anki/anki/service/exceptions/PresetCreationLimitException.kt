package io.github.anki.anki.service.exceptions

class PresetCreationLimitException(
    override val message: String = "Presets limit reached",
    override val cause: Throwable? = null,
) : BaseBadRequestException, RuntimeException(message, cause) {

    companion object {
        fun fromUserId(userId: String?) =
            PresetCreationLimitException("Presets limit reached for user with userId = $userId")
    }
}
