package io.github.anki.anki.controller

class DeckDoesNotExistException(
    message: String = "Deck does not exist",
    cause: Throwable? = null) : RuntimeException(message, cause)
