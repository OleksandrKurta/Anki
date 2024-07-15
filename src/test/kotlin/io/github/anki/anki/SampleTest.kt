package io.github.anki.anki

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SampleTest {

    private val testSample: Sample = Sample()
    @Test
    fun sum() {
        val expected = 42
        println("Test started")
        assertEquals(expected, testSample.sum(40, 2))
    }
}
