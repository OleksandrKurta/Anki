package io.github.anki.anki

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication


@SpringBootApplication
class AnkiApplication

fun main(args: Array<String>) {
	@Suppress("SpreadOperator")
	runApplication<AnkiApplication>(*args)
}
