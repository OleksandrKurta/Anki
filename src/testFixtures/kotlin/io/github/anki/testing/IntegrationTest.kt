package io.github.anki.testing

import io.github.anki.testing.testcontainers.BaseIntegrationTest
import org.junit.jupiter.api.Tag
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ContextConfiguration
import org.testcontainers.junit.jupiter.Testcontainers

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@SpringBootTest
@ContextConfiguration(classes = [BaseIntegrationTest::class])
@Import(BaseIntegrationTest::class)
@Tag("integration")
annotation class IntegrationTest

@IntegrationTest
@AutoConfigureWebTestClient
annotation class IntegrationTestWithClient
