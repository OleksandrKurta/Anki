package io.github.anki.anki

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

class SampleTest {

    private val testSample: Sample = Sample()
    @Test
    fun sum() {
        val expected = 42
        println("Test started")
        assertEquals(expected, testSample.sum(40, 2))
    }
}