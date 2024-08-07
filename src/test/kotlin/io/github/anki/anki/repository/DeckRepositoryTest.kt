package io.github.anki.anki.repository

import io.github.anki.anki.repository.mongodb.DeckRepository
import io.github.anki.anki.repository.mongodb.document.MongoDeck
import io.github.anki.testing.IntegrationTest
import io.github.anki.testing.getRandomID
import io.github.anki.testing.getRandomString
import io.github.anki.testing.testcontainers.TestContainersFactory
import io.github.anki.testing.testcontainers.with
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.util.Assert
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Container
import kotlin.test.BeforeTest

@IntegrationTest
class DeckRepositoryTest @Autowired constructor(
    val deckRepository: DeckRepository,
) {
    private lateinit var newDeck: MongoDeck

    @BeforeTest
    fun setUp() {
        newDeck =
            MongoDeck(
                userId = getRandomID(),
                name = getRandomString(),
                description = getRandomString(),
            )
        LOG.info("Initialized new {}", newDeck)
    }

    @Test
    fun `should insert deck`() {
        // given
        val deckFromMongo = deckRepository.insert(newDeck)

        // when, then
        deckFromMongo.id shouldNotBe null
        Assert.isTrue(deckRepository.existsById(deckFromMongo.id!!), "Deck appear in repository")
    }

    @Test
    fun `should delete existing deck by id`() {
        // given
        val deckFromMongo = deckRepository.insert(newDeck)

        // when
        deckRepository.deleteById(deckFromMongo.id!!)

        // then
        deckRepository.existsById(deckFromMongo.id!!) shouldBe false
    }

    @Test
    fun `should delete NOT existing deck by id`() {
        // given
        val notExistingDeckId = getRandomID()

        // when
        deckRepository.deleteById(notExistingDeckId)

        // then
        deckRepository.existsById(notExistingDeckId) shouldBe false
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(DeckRepositoryTest::class.java)

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
