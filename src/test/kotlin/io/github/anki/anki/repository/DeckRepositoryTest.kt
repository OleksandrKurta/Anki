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
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Container
import reactor.core.publisher.Flux
import reactor.test.StepVerifier
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
        StepVerifier
            .create(
                deckRepository
                    .insert(newDeck)
                    .flatMap { deckRepository.existsByIdWithStatus(it.id!!, DocumentStatus.ACTIVE) },
            )
            .expectNext(true)
            .verifyComplete()
    }

    @Test
    fun `should delete existing deck by id`() {
        StepVerifier
            .create(
                deckRepository.insert(newDeck)
                    .flatMapMany {
                        deckRepository
                            .softDelete(it.id!!)
                            .thenMany(
                                Flux
                                    .zip(
                                        deckRepository.existsByIdWithStatus(it.id!!, DocumentStatus.ACTIVE),
                                        deckRepository.existsByIdWithStatus(it.id!!, DocumentStatus.DELETED),
                                    ),
                            )
                    },
            )
            .assertNext {
                it.t1 shouldBe false
                it.t2 shouldBe true
            }
            .verifyComplete()
    }

    @Test
    fun `should delete NOT existing deck by id`() {
        val notExistingDeckId = getRandomID()
        StepVerifier
            .create(
                deckRepository
                    .softDelete(notExistingDeckId)
                    .then(deckRepository.existsById(notExistingDeckId)),
            )
            .expectNext(false)
            .verifyComplete()
    }

    @Test
    fun `should find deck by UserId with ACTIVE status`() {
        val numberOfDecks = (0..100).random()
        StepVerifier
            .create(
                deckRepository
                    .insertRandom(numberOfDecks, newDeck.userId)
                    .concatWith(deckRepository.findByUserIdWithStatus(newDeck.userId, status = DocumentStatus.ACTIVE))
                    .collectList(),
            )
            .assertNext {
                it.subList(0, numberOfDecks) shouldBe it.subList(numberOfDecks, numberOfDecks * 2)
            }
            .verifyComplete()
    }

    @Test
    fun `should NOT find deck by UserId with DELETED status`() {
        StepVerifier
            .create(
                deckRepository
                    .insert(newDeck)
                    .flatMapMany {
                        deckRepository.findByUserIdWithStatus(it.userId, status = DocumentStatus.DELETED)
                    },
            )
            .expectNextCount(0)
            .verifyComplete()
    }

    @Test
    fun `should return true if deck with Id and UserId exists`() {
        StepVerifier
            .create(
                deckRepository
                    .insert(newDeck)
                    .flatMap {
                        deckRepository.existsByIdAndUserIdWithStatus(
                            it.id!!,
                            it.userId,
                            status = DocumentStatus.ACTIVE,
                        )
                    },
            )
            .expectNext(true)
            .verifyComplete()
    }

    @Test
    fun `should return false if deck with Id and UserId not exists`() {
        StepVerifier
            .create(
                deckRepository
                    .insertRandom(5, newDeck.userId)
                    .then(
                        deckRepository.existsByIdAndUserIdWithStatus(
                            getRandomID(),
                            newDeck.userId,
                            status = DocumentStatus.DELETED,
                        )
                    )
            )
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
