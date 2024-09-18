package io.github.anki.anki.service.utils

import io.kotest.assertions.throwables.shouldThrowExactly
import io.mockk.every
import io.mockk.mockk
import org.springframework.dao.DuplicateKeyException
import java.util.concurrent.ExecutionException
import java.util.concurrent.Future
import kotlin.test.Test

class FutureUtilsTest {

    @Test
    fun `should throw cause if ex cause is not null`() {
        // given
        val future: Future<*> = mockk()

        every { future.get() } throws ExecutionException("test message", DuplicateKeyException("test message"))

        // when/then
        shouldThrowExactly<DuplicateKeyException> {
            future.getOrThrowCause()
        }
    }

    @Test
    fun `should throw ExecutionException if cause is null`() {
        // given
        val future: Future<*> = mockk()

        every { future.get() } throws ExecutionException("test message", null)

        // when/then
        shouldThrowExactly<ExecutionException> {
            future.getOrThrowCause()
        }
    }
}
