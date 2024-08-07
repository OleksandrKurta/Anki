package io.github.anki.testing

import org.junit.jupiter.api.Tag
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.testcontainers.junit.jupiter.Testcontainers

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Testcontainers
@SpringBootTest
@Tag("integration")
annotation class IntegrationTest

@IntegrationTest
@AutoConfigureMockMvc
annotation class MVCTest
