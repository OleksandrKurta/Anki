package io.github.anki.anki.controller.exceptions

class CardDoesNotExistException(
    override val message: String = "Card does not exist",
    override val cause: Throwable? = null,
) : DoesNotExist, RuntimeException(message, cause)
