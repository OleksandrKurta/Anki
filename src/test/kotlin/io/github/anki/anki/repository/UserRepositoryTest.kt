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
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Container
import kotlin.test.BeforeTest

@IntegrationTest
class UserRepositoryTest @Autowired constructor(
    val userRepository: UserRepository,
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
        // when
        val userFromMongo = userRepository.insert(newUser).get()
        // then
        userFromMongo.id shouldNotBe null
        userRepository.existsByIdWithStatus(userFromMongo.id!!, DocumentStatus.ACTIVE).get() shouldBe true
    }

    @Test
    fun `should find existing user by userName`() {
        // given
        val userFromMongo = userRepository.insert(newUser).get()

        // when
        val userFromFind = userFromMongo.userName?.let { userRepository.findByUserName(userName = it).get() }

        // then
        userFromFind!!.userName shouldBe userFromMongo.userName
    }

    @Test
    fun `should return null for non-existent user by userName`() {
        // given
        val userName = getRandomString()
        // when
        val userFromFind = userRepository.findByUserName(userName = userName).get()
        // then
        userFromFind shouldBe null
    }

    @Test
    fun `should return true for existsByUserName when user exist`() {
        // given
        val userFromMongo = userRepository.insert(newUser).get()
        // then
        userRepository.existsByUserName(userFromMongo.userName).get() shouldBe true
    }

    @Test
    fun `should return false for existsByUserName when user not exist `() {
        // given
        val notExistingUserName = getRandomString()
        // then
        userRepository.existsByUserName(notExistingUserName).get() shouldBe false
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
