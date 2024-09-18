package io.github.anki.anki.service

import io.github.anki.anki.repository.mongodb.CardRepository
import io.github.anki.anki.repository.mongodb.DeckRepository
import io.github.anki.anki.repository.mongodb.document.DocumentStatus
import io.github.anki.anki.repository.mongodb.document.MongoDeck
import io.github.anki.anki.service.exceptions.DeckDoesNotExistException
import io.github.anki.anki.service.model.Deck
import io.github.anki.anki.service.model.mapper.toDeck
import io.github.anki.anki.service.model.mapper.toMongo
import io.github.anki.testing.DATA_PREFIX
import io.github.anki.testing.getRandomID
import io.github.anki.testing.getRandomString
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.bson.types.ObjectId
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.util.concurrent.CompletableFuture
import kotlin.test.Test

@ExtendWith(MockKExtension::class)
class DeckServiceTest {

    @MockK
    lateinit var deckRepository: DeckRepository

    @MockK
    lateinit var cardRepository: CardRepository

    @InjectMockKs
    lateinit var deckService: DeckService

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Nested
    @DisplayName("DeckService.createNewDeck()")
    @TestInstance(Lifecycle.PER_CLASS)
    inner class CreateNewDeck {
        @Test
        fun `should create new deck always`() {
            // given
            val userId = getRandomID()
            val deck =
                Deck(
                    userId = userId.toHexString(),
                    name = "name",
                    description = "description",
                )
            val mongoDeck =
                MongoDeck(
                    userId = userId,
                    name = deck.name,
                    description = deck.description,
                )
            val createdMongoDeck = mongoDeck.copy(id = getRandomID())
            val expectedDeck = deck.copy(id = createdMongoDeck.id!!.toHexString())
            every { deckRepository.insert(mongoDeck) } returns CompletableFuture.completedFuture(createdMongoDeck)

            // when
            val actual: Deck = deckService.createNewDeck(deck)

            // then
            actual shouldBe expectedDeck

            verify(exactly = 1) {
                deckRepository.insert(mongoDeck)
            }
        }
    }

    @Nested
    @DisplayName("DeckService.getDecks()")
    @TestInstance(Lifecycle.PER_CLASS)
    inner class GetDecks {

        @ParameterizedTest
        @ValueSource(ints = [0, 1, 20, 100])
        fun `should return all decks if they exist`(numberOfDecks: Int) {
            // given
            val userId = getRandomID()

            val randomDecks = getRandomMongoDecks(numberOfDecks, userId)

            every {
                deckRepository.findByUserIdWithStatus(userId)
            } returns CompletableFuture.completedFuture(randomDecks)
            val expectedDecks = randomDecks.map { it.toDeck() }

            // when
            val actualDecks = deckService.getDecks(userId.toString())

            // then
            actualDecks.size shouldBe numberOfDecks
            actualDecks shouldContainExactlyInAnyOrder expectedDecks

            verify(exactly = 1) { deckRepository.findByUserIdWithStatus(userId) }
        }

        private fun getRandomMongoDecks(number: Int, userId: ObjectId): List<MongoDeck> {
            val mongoDecks: MutableCollection<MongoDeck> = mutableListOf()
            repeat(number) {
                mongoDecks.add(
                    MongoDeck(
                        id = getRandomID(),
                        userId = userId,
                        name = getRandomString(DATA_PREFIX),
                        description = getRandomString(DATA_PREFIX),
                    ),
                )
            }
            return mongoDecks.toList()
        }
    }

    @Nested
    @DisplayName("DeckService.updateDeck()")
    @TestInstance(Lifecycle.PER_CLASS)
    inner class UpdateDeck {
        @Test
        fun `should update deck is it exists`() {
            // given
            val initialDeck =
                Deck(
                    id = getRandomID().toString(),
                    userId = getRandomID().toString(),
                    name = getRandomString(DATA_PREFIX),
                    description = getRandomString(DATA_PREFIX),
                )
            val updatedDeck =
                Deck(
                    id = initialDeck.id,
                    userId = initialDeck.userId,
                    name = getRandomString("updated"),
                    description = getRandomString("updated"),
                )

            every {
                deckRepository.existsByIdAndUserIdWithStatus(
                    ObjectId(initialDeck.id),
                    ObjectId(initialDeck.userId),
                    DocumentStatus.ACTIVE,
                )
            } returns CompletableFuture.completedFuture(true)

            every {
                deckRepository.findByIdAndUserIdWithStatus(ObjectId(initialDeck.id), ObjectId(initialDeck.userId))
            } returns CompletableFuture.completedFuture(initialDeck.toMongo())

            every {
                deckRepository.save(updatedDeck.toMongo())
            } returns CompletableFuture.completedFuture(updatedDeck.toMongo())

            // when
            val actualDeck = deckService.updateDeck(updatedDeck)

            // then
            actualDeck shouldBe updatedDeck

            verify(exactly = 1) {
                deckRepository.existsByIdAndUserIdWithStatus(
                    ObjectId(initialDeck.id),
                    ObjectId(initialDeck.userId),
                    DocumentStatus.ACTIVE,
                )
            }

            verify(exactly = 1) {
                deckRepository.findByIdAndUserIdWithStatus(
                    ObjectId(initialDeck.id!!),
                    ObjectId(initialDeck.userId),
                )
            }

            verify(exactly = 1) {
                deckRepository.save(updatedDeck.toMongo())
            }
        }

        @Test
        fun `should be error if deck was not found`() {
            // given
            every {
                deckRepository.existsByIdAndUserIdWithStatus(
                    any(),
                    any(),
                    DocumentStatus.ACTIVE,
                )
            } returns CompletableFuture.completedFuture(true)

            every {
                deckRepository.findByIdAndUserIdWithStatus(any(), any())
            } returns CompletableFuture.completedFuture(null)

            // when/then
            shouldThrowExactly<DeckDoesNotExistException> {
                deckService.updateDeck(
                    Deck(
                        id = getRandomID().toString(),
                        userId = getRandomID().toString(),
                        name = getRandomString(),
                        description = getRandomString(),
                    ),
                )
            }
        }

        @Test
        fun `should be error if deckId is null`() {
            // when/then
            shouldThrowExactly<IllegalArgumentException> {
                deckService.updateDeck(
                    Deck(
                        id = null,
                        userId = getRandomID().toString(),
                        name = getRandomString(),
                        description = getRandomString(),
                    ),
                )
            }
        }

        @Test
        fun `should change nothing in all fields is actual`() {
            // given
            val initialDeck =
                Deck(
                    id = getRandomID().toString(),
                    userId = getRandomID().toString(),
                    name = getRandomString("initial"),
                    description = getRandomString("initial"),
                )

            every {
                deckRepository.existsByIdAndUserIdWithStatus(
                    ObjectId(initialDeck.id),
                    ObjectId(initialDeck.userId),
                    DocumentStatus.ACTIVE,
                ).get()
            } returns true

            every {
                deckRepository.findByIdAndUserIdWithStatus(ObjectId(initialDeck.id), ObjectId(initialDeck.userId))
            } returns CompletableFuture.completedFuture(initialDeck.toMongo())

            // when
            val actualDeck = deckService.updateDeck(initialDeck)

            // then
            actualDeck shouldBe initialDeck

            verify(exactly = 1) {
                deckRepository.existsByIdAndUserIdWithStatus(
                    ObjectId(initialDeck.id),
                    ObjectId(initialDeck.userId),
                    DocumentStatus.ACTIVE,
                )
            }

            verify(exactly = 1) {
                deckRepository.findByIdAndUserIdWithStatus(
                    ObjectId(initialDeck.id!!),
                    ObjectId(initialDeck.userId),
                )
            }

            verify(exactly = 0) {
                deckRepository.save(any())
            }
        }

        @Test
        fun `should change nothing if all fields is null`() {
            // given
            val initialDeck =
                Deck(
                    id = getRandomID().toString(),
                    userId = getRandomID().toString(),
                    name = getRandomString("initial"),
                    description = getRandomString("initial"),
                )
            val updatedDeck =
                Deck(
                    id = initialDeck.id,
                    userId = initialDeck.userId,
                    name = null,
                    description = null,
                )

            every {
                deckRepository.existsByIdAndUserIdWithStatus(
                    ObjectId(initialDeck.id),
                    ObjectId(initialDeck.userId),
                    DocumentStatus.ACTIVE,
                )
            } returns CompletableFuture.completedFuture(true)

            every {
                deckRepository.findByIdAndUserIdWithStatus(ObjectId(initialDeck.id), ObjectId(initialDeck.userId))
            } returns CompletableFuture.completedFuture(initialDeck.toMongo())

            // when
            val actualDeck = deckService.updateDeck(updatedDeck)

            // then
            actualDeck shouldBe initialDeck

            verify(exactly = 1) {
                deckRepository.existsByIdAndUserIdWithStatus(
                    ObjectId(initialDeck.id),
                    ObjectId(initialDeck.userId),
                    DocumentStatus.ACTIVE,
                )
            }

            verify(exactly = 1) {
                deckRepository.findByIdAndUserIdWithStatus(
                    ObjectId(initialDeck.id!!),
                    ObjectId(initialDeck.userId),
                )
            }

            verify(exactly = 0) {
                deckRepository.save(any())
            }
        }

        @Test
        fun `should update only name`() {
            // given
            val initialDeck =
                Deck(
                    id = getRandomID().toString(),
                    userId = getRandomID().toString(),
                    name = getRandomString("initial"),
                    description = getRandomString("initial"),
                )
            val updatedDeck =
                Deck(
                    id = initialDeck.id,
                    userId = initialDeck.userId,
                    name = getRandomString("updated"),
                    description = null,
                )
            val expectedDeck =
                Deck(
                    id = getRandomID().toString(),
                    userId = getRandomID().toString(),
                    name = updatedDeck.name,
                    description = initialDeck.description,
                )

            every {
                deckRepository.existsByIdAndUserIdWithStatus(
                    ObjectId(initialDeck.id),
                    ObjectId(initialDeck.userId),
                    DocumentStatus.ACTIVE,
                )
            } returns CompletableFuture.completedFuture(true)
            every {
                deckRepository.findByIdAndUserIdWithStatus(ObjectId(initialDeck.id), ObjectId(initialDeck.userId))
            } returns CompletableFuture.completedFuture(initialDeck.toMongo())

            every {
                deckRepository.save(any())
            } returns CompletableFuture.completedFuture(expectedDeck.toMongo())

            // when
            val actualDeck = deckService.updateDeck(updatedDeck)

            // then
            actualDeck shouldBe expectedDeck

            verify(exactly = 1) {
                deckRepository.existsByIdAndUserIdWithStatus(
                    ObjectId(initialDeck.id),
                    ObjectId(initialDeck.userId),
                    DocumentStatus.ACTIVE,
                )
            }

            verify(exactly = 1) {
                deckRepository.findByIdAndUserIdWithStatus(
                    ObjectId(initialDeck.id!!),
                    ObjectId(initialDeck.userId),
                )
            }

            verify(exactly = 1) {
                deckRepository.save(any())
            }
        }

        @Test
        fun `should update only description`() {
            // given
            val initialDeck =
                Deck(
                    id = getRandomID().toString(),
                    userId = getRandomID().toString(),
                    name = getRandomString("initial"),
                    description = getRandomString("initial"),
                )
            val updatedDeck =
                Deck(
                    id = initialDeck.id,
                    userId = initialDeck.userId,
                    name = null,
                    description = getRandomString("updated"),
                )
            val expectedDeck =
                Deck(
                    id = getRandomID().toString(),
                    userId = getRandomID().toString(),
                    name = initialDeck.name,
                    description = updatedDeck.description,
                )

            every {
                deckRepository.existsByIdAndUserIdWithStatus(
                    ObjectId(initialDeck.id),
                    ObjectId(initialDeck.userId),
                    DocumentStatus.ACTIVE,
                )
            } returns CompletableFuture.completedFuture(true)

            every {
                deckRepository.findByIdAndUserIdWithStatus(ObjectId(initialDeck.id), ObjectId(initialDeck.userId))
            } returns CompletableFuture.completedFuture(initialDeck.toMongo())

            every {
                deckRepository.save(any())
            } returns CompletableFuture.completedFuture(expectedDeck.toMongo())

            // when
            val actualDeck = deckService.updateDeck(updatedDeck)

            // then
            actualDeck shouldBe expectedDeck

            verify(exactly = 1) {
                deckRepository.existsByIdAndUserIdWithStatus(
                    ObjectId(initialDeck.id),
                    ObjectId(initialDeck.userId),
                    DocumentStatus.ACTIVE,
                )
            }

            verify(exactly = 1) {
                deckRepository.findByIdAndUserIdWithStatus(
                    ObjectId(initialDeck.id!!),
                    ObjectId(initialDeck.userId),
                )
            }

            verify(exactly = 1) {
                deckRepository.save(any())
            }
        }
    }

    @Nested
    @DisplayName("DeckService.deleteDeck()")
    @TestInstance(Lifecycle.PER_CLASS)
    inner class DeleteDeck {
        @Test
        fun `should delete the deck and all cards`() {
            // given
            val deckId = getRandomID()
            val userId = getRandomID()

            every {
                deckRepository.existsByIdAndUserIdWithStatus(
                    deckId,
                    userId,
                    DocumentStatus.ACTIVE,
                )
            } returns CompletableFuture.completedFuture(true)

            every { deckRepository.softDelete(deckId) } returns CompletableFuture.completedFuture(null)
            every { cardRepository.softDeleteByDeckId(deckId) } returns CompletableFuture.completedFuture(null)

            // when
            deckService.deleteDeck(deckId.toString(), userId.toString())

            // then

            verify(exactly = 1) {
                deckRepository.existsByIdAndUserIdWithStatus(
                    deckId,
                    userId,
                    DocumentStatus.ACTIVE,
                )
            }
            verify(exactly = 1) { deckRepository.softDelete(deckId) }
            verify(exactly = 1) { cardRepository.softDeleteByDeckId(deckId) }
        }
    }
}
