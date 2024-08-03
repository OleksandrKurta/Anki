package io.github.anki.anki.controller.exceptions

class DeckDoesNotExistException(
    override val message: String = "Deck does not exist",
    override val cause: Throwable? = null) : DoesNotExist, RuntimeException(message, cause)
