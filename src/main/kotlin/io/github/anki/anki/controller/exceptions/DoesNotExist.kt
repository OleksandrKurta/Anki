package io.github.anki.anki.controller.exceptions

interface DoesNotExist {
    val message: String
    val cause: Throwable?
}
