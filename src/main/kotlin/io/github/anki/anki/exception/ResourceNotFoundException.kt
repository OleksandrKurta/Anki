package io.github.anki.anki.exception


class ResourceNotFoundException(message: String? = null,
                                cause: Throwable? = null) : RuntimeException(message, cause)
