package io.github.anki.anki.service

import io.github.anki.anki.repository.mongodb.CardRepository
import io.github.anki.anki.repository.mongodb.DeckRepository
import io.github.anki.anki.repository.mongodb.document.MongoDeck
import io.github.anki.anki.service.model.Deck
import io.github.anki.anki.service.model.mapper.toDeck
import io.github.anki.anki.service.model.mapper.toMongo
import io.github.anki.testing.getRandomID
import io.github.anki.testing.getRandomString
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
import kotlin.test.Test

@ExtendWith(MockKExtension::class)
class DeckServiceTest {

    @MockK
    lateinit var deckRepository: DeckRepository

    @MockK
    lateinit var cardRepository: CardRepository

    @InjectMockKs
    lateinit var sut: DeckService

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
            // GIVEN
            val userId = ObjectId()
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
            val createdMongoDeck = mongoDeck.copy(id = ObjectId())
            val expectedDeck = deck.copy(id = createdMongoDeck.id!!.toHexString())
            every { deckRepository.insert(mongoDeck) } returns createdMongoDeck

            // WHEN
            val actual: Deck = sut.createNewDeck(deck)

            // THEN
            actual shouldBe expectedDeck

            // AND
            verify(exactly = 1) {
                deckRepository.insert(mongoDeck)
            }
        }
    }

    @Nested
    @DisplayName("DeckService.getDecks()")
    @TestInstance(Lifecycle.PER_CLASS)
    inner class GetDecks {
        @Test
        fun `should return all decks if they exist`() {
            // given
            val userId = ObjectId()

            val randomDecks = getRandomMongoDecks((1..100).random(), userId)

            every { deckRepository.findByUserId(userId) } returns randomDecks
            val expectedDecks = randomDecks.map { it.toDeck() }

            // when
            val actualDecks = sut.getDecks(userId.toString())

            // then
            actualDecks shouldBe expectedDecks

            verify(exactly = 1) { deckRepository.findByUserId(userId) }
        }

        @Test
        fun `should return empty list if there is no decks`() {
            // given
            val userId = ObjectId()

            every { deckRepository.findByUserId(userId) } returns emptyList()

            // when
            val actualDecks = sut.getDecks(userId.toString())

            // then
            actualDecks.isEmpty() shouldBe true

            verify(exactly = 1) { deckRepository.findByUserId(userId) }
        }

        private fun getRandomMongoDecks(number: Int, userId: ObjectId): List<MongoDeck> {
            val mongoDecks: MutableCollection<MongoDeck> = mutableListOf()
            repeat(number) {
                mongoDecks.add(
                    MongoDeck(
                        id = ObjectId(),
                        userId = userId,
                        name = getRandomString(),
                        description = getRandomString(),
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
                    name = getRandomString("initial"),
                    description = getRandomString("initial"),
                )
            val updatedDeck =
                Deck(
                    id = initialDeck.id,
                    userId = initialDeck.userId,
                    name = getRandomString("updated"),
                    description = getRandomString("updated"),
                )

            every {
                deckRepository.findByIdAndUserId(ObjectId(initialDeck.id), ObjectId(initialDeck.userId))
            } returns initialDeck.toMongo()

            every {
                deckRepository.save(updatedDeck.toMongo())
            } returns updatedDeck.toMongo()

            // when
            val actualDeck = sut.updateDeck(updatedDeck)

            // then
            actualDeck shouldBe updatedDeck

            verify(exactly = 1) {
                sut.getDeckByIdAndUserId(initialDeck.id!!, initialDeck.userId)
            }

            verify(exactly = 1) {
                deckRepository.save(updatedDeck.toMongo())
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
                deckRepository.findByIdAndUserId(ObjectId(initialDeck.id), ObjectId(initialDeck.userId))
            } returns initialDeck.toMongo()

            val updatedDeck = initialDeck

            // when
            val actualDeck = sut.updateDeck(updatedDeck)

            // then
            actualDeck shouldBe updatedDeck

            verify(exactly = 1) {
                sut.getDeckByIdAndUserId(initialDeck.id!!, initialDeck.userId)
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
                deckRepository.findByIdAndUserId(ObjectId(initialDeck.id), ObjectId(initialDeck.userId))
            } returns initialDeck.toMongo()

            // when
            val actualDeck = sut.updateDeck(updatedDeck)

            // then
            actualDeck shouldBe updatedDeck

            verify(exactly = 1) {
                sut.getDeckByIdAndUserId(initialDeck.id!!, initialDeck.userId)
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
                deckRepository.findByIdAndUserId(ObjectId(initialDeck.id), ObjectId(initialDeck.userId))
            } returns initialDeck.toMongo()

            every {
                deckRepository.save(any())
            } returns expectedDeck.toMongo()

            // when
            val actualDeck = sut.updateDeck(updatedDeck)

            // then
            actualDeck shouldBe expectedDeck

            verify(exactly = 1) {
                sut.getDeckByIdAndUserId(initialDeck.id!!, initialDeck.userId)
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
                deckRepository.findByIdAndUserId(ObjectId(initialDeck.id), ObjectId(initialDeck.userId))
            } returns initialDeck.toMongo()

            every {
                deckRepository.save(any())
            } returns expectedDeck.toMongo()

            // when
            val actualDeck = sut.updateDeck(updatedDeck)

            // then
            actualDeck shouldBe expectedDeck

            verify(exactly = 1) {
                sut.getDeckByIdAndUserId(initialDeck.id!!, initialDeck.userId)
            }

            verify(exactly = 1) {
                deckRepository.save(any())
            }
        }
    }
}
