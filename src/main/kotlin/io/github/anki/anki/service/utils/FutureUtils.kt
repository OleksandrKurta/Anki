package io.github.anki.anki.service.utils

import java.util.concurrent.ExecutionException
import java.util.concurrent.Future

fun <T> Future<T>.getOrThrowCause(): T {
    try {
        return this.get()
    } catch (ex: ExecutionException) {
        throw ex.cause ?: ex
    }
}
