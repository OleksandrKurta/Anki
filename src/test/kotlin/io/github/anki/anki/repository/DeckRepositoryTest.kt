package io.github.anki.anki.repository

import io.github.anki.anki.repository.mongodb.DeckRepository
import io.github.anki.anki.repository.mongodb.document.DocumentStatus
import io.github.anki.anki.repository.mongodb.document.MongoDeck
import io.github.anki.testing.IntegrationTest
import io.github.anki.testing.getRandomID
import io.github.anki.testing.getRandomString
import io.github.anki.testing.insertRandom
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
    }

    @Test
    fun `should insert deck`() {
        // given
        val deckFromMongo = deckRepository.insert(newDeck)

        // when, then
        deckFromMongo.id shouldNotBe null

        deckRepository.existsByIdWithStatus(deckFromMongo.id!!, DocumentStatus.ACTIVE) shouldBe true
    }

    @Test
    fun `should delete existing deck by id`() {
        // given
        val deckFromMongo = deckRepository.insert(newDeck)

        // when
        deckRepository.softDelete(deckFromMongo.id!!)

        // then
        deckRepository.existsByIdWithStatus(deckFromMongo.id!!, DocumentStatus.ACTIVE) shouldBe false
        deckRepository.existsByIdWithStatus(deckFromMongo.id!!, DocumentStatus.DELETED) shouldBe true
    }

    @Test
    fun `should delete NOT existing deck by id`() {
        // given
        val notExistingDeckId = getRandomID()

        // when
        deckRepository.softDelete(notExistingDeckId)

        // then
        deckRepository.existsById(notExistingDeckId) shouldBe false
    }

    @Test
    fun `should find deck by UserId with ACTIVE status`() {
        // given
        val randomDecks = deckRepository.insertRandom(5, newDeck.userId)

        // when
        val decks = deckRepository.findByUserIdWithStatus(newDeck.userId, status = DocumentStatus.ACTIVE)
        decks.size shouldBe 5
        // then
        decks shouldBe randomDecks
    }

    @Test
    fun `should NOT find deck by UserId with DELETED status`() {
        // given
        val deckFromMongo = deckRepository.insert(newDeck)

        // when
        val deck = deckRepository.findByUserIdWithStatus(deckFromMongo.userId, status = DocumentStatus.DELETED)

        // then
        deck shouldBe listOf()
    }

    @Test
    fun `should return true if deck with Id and UserId exists`() {
        // given
        val deckFromMongo = deckRepository.insert(newDeck)
        deckRepository.insertRandom(5, newDeck.userId)
        // when
        val deck =
            deckFromMongo.id?.let {
                deckRepository.existsByIdAndUserIdWithStatus(
                    it,
                    deckFromMongo.userId,
                    status = DocumentStatus.ACTIVE,
                )
            }

        // then
        deck shouldBe true
    }

    @Test
    fun `should return false if deck with Id and UserId not exists`() {
        // when
        deckRepository.insertRandom(5, newDeck.userId)
        val deck =
            deckRepository.existsByIdAndUserIdWithStatus(
                getRandomID(),
                newDeck.userId,
                status = DocumentStatus.DELETED,
            )

        // then
        deck shouldBe false
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
