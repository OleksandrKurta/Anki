package io.github.anki.anki.repository

import io.github.anki.anki.repository.mongodb.UserRepository
import io.github.anki.anki.repository.mongodb.document.DocumentStatus
import io.github.anki.anki.repository.mongodb.document.MongoUser
import io.github.anki.testing.IntegrationTest
import io.github.anki.testing.getRandomEmail
import io.github.anki.testing.getRandomString
import io.github.anki.testing.testcontainers.TestContainersFactory
import io.github.anki.testing.testcontainers.with
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Container
import reactor.test.StepVerifier
import kotlin.test.BeforeTest

@IntegrationTest
class UserRepositoryTest @Autowired constructor(
    private val userRepository: UserRepository,
) {
    private lateinit var newUser: MongoUser

    @BeforeTest
    fun setUp() {
        newUser =
            MongoUser(
                userName = getRandomString(),
                email = getRandomEmail(),
                password = getRandomString(),
            )
    }

    @Test
    fun `should insert user`() {
        StepVerifier
            .create(
                userRepository
                    .insert(newUser)
                    .flatMap { userRepository.existsByIdWithStatus(it.id!!, DocumentStatus.ACTIVE) },
            )
            .expectNext(true)
            .verifyComplete()
    }

    @Test
    fun `should find existing user by userName`() {
        StepVerifier
            .create(
                userRepository
                    .insert(newUser)
                    .flatMap { userRepository.findByUserName(userName = it.userName!!) },
            )
            .assertNext {
                it.userName shouldBe newUser.userName
            }
            .verifyComplete()
    }

    @Test
    fun `should return null for non-existent user by userName`() {
        StepVerifier
            .create(userRepository.findByUserName(userName = getRandomString()))
            .expectNextCount(0)
            .verifyComplete()
    }

    @Test
    fun `should return true for existsByUserName when user exist`() {
        StepVerifier
            .create(
                userRepository.insert(newUser)
                    .flatMap { userRepository.existsByUserName(it.userName) },
            )
            .expectNext(true)
            .verifyComplete()
    }

    @Test
    fun `should return false for existsByUserName when user not exist `() {
        StepVerifier
            .create(userRepository.existsByUserName(getRandomString()))
            .expectNext(false)
            .verifyComplete()
    }

    companion object {
        @Container
        @Suppress("PropertyName")
        private val mongoDBContainer: MongoDBContainer = TestContainersFactory.newMongoContainer()

        @DynamicPropertySource
        @JvmStatic
        fun setProperties(registry: DynamicPropertyRegistry) {
            registry.with(mongoDBContainer)
        }
    }
}
