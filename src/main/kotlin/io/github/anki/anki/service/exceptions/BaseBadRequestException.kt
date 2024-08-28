package io.github.anki.anki.service.exceptions

interface BaseBadRequestException {
    val message: String
    val cause: Throwable?
}
