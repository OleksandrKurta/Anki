package io.github.anki.anki.service.exceptions

interface DoesNotExist {
    val message: String
    val cause: Throwable?
}
