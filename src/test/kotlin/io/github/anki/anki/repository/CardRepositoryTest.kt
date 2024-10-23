package io.github.anki.anki.repository

import io.github.anki.anki.repository.mongodb.CardRepository
import io.github.anki.anki.repository.mongodb.document.DocumentStatus
import io.github.anki.anki.repository.mongodb.document.MongoCard
import io.github.anki.testing.IntegrationTest
import io.github.anki.testing.getRandomID
import io.github.anki.testing.getRandomString
import io.github.anki.testing.testcontainers.TestContainersFactory
import io.github.anki.testing.testcontainers.with
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import reactor.util.context.Context
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
class CardRepositoryTest @Autowired constructor(
    val cardRepository: CardRepository,
) {
    private lateinit var newCard: MongoCard

    @BeforeTest
    fun setUp() {
        newCard =
            MongoCard(
                deckId = getRandomID(),
                key = getRandomString(),
                value = getRandomString(),
            )
    }

    @Test
    fun `should insert card`() {
        StepVerifier
            .create(
                cardRepository
                    .insert(newCard)
                    .flatMap { cardRepository.existsByIdWithStatus(it.id!!, DocumentStatus.ACTIVE) }
            )
            .expectNext(true)
            .verifyComplete()
    }

    @Test
    fun `should soft delete existing card by id`() {
        StepVerifier
            .create(
                cardRepository
                    .insert(newCard)
                    .flatMapMany {
                        cardRepository.softDelete(it.id!!)
                            .thenMany(
                                Flux
                                    .zip(
                                        cardRepository.existsByIdWithStatus(it.id!!, DocumentStatus.ACTIVE),
                                        cardRepository.existsByIdWithStatus(it.id!!, DocumentStatus.DELETED),
                                    )
                            )
                    }
            )
            .assertNext {
                it.t1 shouldBe false
                it.t2 shouldBe true
            }
            .verifyComplete()
    }

    @Test
    fun `should delete NOT existing card by id`() {
        val notExistingCardId = getRandomID()
        StepVerifier
            .create(
                cardRepository.softDelete(notExistingCardId)
                    .then(cardRepository.existsById(notExistingCardId))
            )
            .expectNext(false)
            .verifyComplete()
    }

    @Test
    fun `should NOT find card by not exist deckId`() {
        StepVerifier
            .create(cardRepository.findByDeckIdWithStatus(deckId = getRandomID()))
            .expectNextCount(0)
            .verifyComplete()
    }

    @Test
    fun `should find card by deckId`() {
        StepVerifier
            .create(
                cardRepository
                    .insert(newCard)
                    .concatWith(cardRepository.findByDeckIdWithStatus(deckId = newCard.deckId!!))
                    .buffer(2)
            )
            .assertNext {
                it[0] shouldBe it[1]
            }
            .verifyComplete()
    }

    @Test
    fun `should soft delete by card by deckId`() {
        StepVerifier
            .create(
                cardRepository
                    .insert(newCard)
                    .flatMap {
                        cardRepository
                            .softDeleteByDeckId(deckId = it.deckId!!)
                            .then(cardRepository.findByIdWithStatus(it.id!!, DocumentStatus.ACTIVE))
                    }
            )
            .expectNextCount(0)
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
