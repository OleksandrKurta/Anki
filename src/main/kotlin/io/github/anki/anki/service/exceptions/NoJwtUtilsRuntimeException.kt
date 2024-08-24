package io.github.anki.anki.service.exceptions

class NoJwtUtilsRuntimeException(
    override val message: String = "No jwtUtils was initialized",
    override val cause: Throwable? = null,
) : RuntimeException(message, cause)
