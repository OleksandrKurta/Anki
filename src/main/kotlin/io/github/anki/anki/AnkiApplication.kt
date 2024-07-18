package io.github.anki.anki

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.RestController


@SpringBootApplication
@RestController("/")
class AnkiApplication

fun main(args: Array<String>) {
	@Suppress("SpreadOperator")
	runApplication<AnkiApplication>(*args)
}
