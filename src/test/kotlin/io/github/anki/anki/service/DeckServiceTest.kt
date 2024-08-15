package io.github.anki.anki.service

import io.github.anki.anki.repository.mongodb.CardRepository
import io.github.anki.anki.repository.mongodb.DeckRepository
import io.github.anki.anki.repository.mongodb.document.DocumentStatus
import io.github.anki.anki.repository.mongodb.document.MongoDeck
import io.github.anki.anki.service.exceptions.DeckDoesNotExistException
import io.github.anki.anki.service.model.Deck
import io.github.anki.anki.service.model.mapper.toDeck
import io.github.anki.anki.service.model.mapper.toMongo
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
import kotlinx.coroutines.runBlocking
import org.bson.types.ObjectId
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
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
            every { runBlocking { deckRepository.insert(mongoDeck) } } returns createdMongoDeck

            // when
            val actual: Deck = runBlocking { deckService.createNewDeck(deck) }

            // then
            actual shouldBe expectedDeck

            verify(exactly = 1) {
                runBlocking { deckRepository.insert(mongoDeck) }
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
            val userId = ObjectId()

            val randomDecks = getRandomMongoDecks(numberOfDecks, userId)

            every {
                runBlocking { deckRepository.findByUserIdWithStatus(userId) }
            } returns randomDecks
            val expectedDecks = randomDecks.map { it.toDeck() }

            // when
            val actualDecks = runBlocking { deckService.getDecks(userId.toString()) }

            // then
            actualDecks.size shouldBe numberOfDecks
            actualDecks shouldContainExactlyInAnyOrder expectedDecks

            verify(exactly = 1) {
                runBlocking { deckRepository.findByUserIdWithStatus(userId) }
            }
        }

        private fun getRandomMongoDecks(number: Int, userId: ObjectId): List<MongoDeck> {
            val mongoDecks: MutableCollection<MongoDeck> = mutableListOf()
            repeat(number) {
                mongoDecks.add(
                    MongoDeck(
                        id = ObjectId(),
                        userId = userId,
                        name = getRandomString("initial"),
                        description = getRandomString("initial"),
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
                runBlocking {
                    deckRepository.existsByIdAndUserIdWithStatus(
                        ObjectId(initialDeck.id),
                        ObjectId(initialDeck.userId),
                        DocumentStatus.ACTIVE,
                    )
                }
            } returns true

            mockFindByIdAndUserIdWithStatus(initialDeck.id!!, initialDeck.userId, initialDeck.toMongo())

            every {
                runBlocking { deckRepository.save(updatedDeck.toMongo()) }
            } returns updatedDeck.toMongo()

            // when
            val actualDeck = runBlocking { deckService.updateDeck(updatedDeck) }

            // then
            actualDeck shouldBe updatedDeck

            verify(exactly = 1) {
                runBlocking {
                    deckRepository.existsByIdAndUserIdWithStatus(
                        ObjectId(initialDeck.id),
                        ObjectId(initialDeck.userId),
                        DocumentStatus.ACTIVE,
                    )
                }
            }

            verify(exactly = 1) {
                runBlocking {
                    deckRepository.findByIdAndUserIdWithStatus(ObjectId(initialDeck.id!!), ObjectId(initialDeck.userId))
                }
            }

            verify(exactly = 1) {
                runBlocking { deckRepository.save(updatedDeck.toMongo()) }
            }
        }

        @Test
        fun `should be error if deck was not found`() {
            // given
            every {
                runBlocking {
                    deckRepository.existsByIdAndUserIdWithStatus(
                        any(),
                        any(),
                        DocumentStatus.ACTIVE,
                    )
                }
            } returns true

            every {
                runBlocking { deckRepository.findByIdAndUserIdWithStatus(any(), any()) }
            } returns null

            // when/then
            shouldThrowExactly<DeckDoesNotExistException> {
                runBlocking {
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
        }

        @Test
        fun `should be error if deckId is null`() {
            // when/then
            shouldThrowExactly<IllegalArgumentException> {
                runBlocking {
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
                runBlocking {
                    deckRepository.existsByIdAndUserIdWithStatus(
                        any(),
                        any(),
                        DocumentStatus.ACTIVE,
                    )
                }
            } returns true

            mockFindByIdAndUserIdWithStatus(initialDeck.id!!, initialDeck.userId, initialDeck.toMongo())

            // when
            val actualDeck = runBlocking { deckService.updateDeck(initialDeck) }

            // then
            actualDeck shouldBe initialDeck

            verify(exactly = 1) {
                runBlocking {
                    deckRepository.existsByIdAndUserIdWithStatus(
                        ObjectId(initialDeck.id),
                        ObjectId(initialDeck.userId),
                        DocumentStatus.ACTIVE,
                    )
                }
            }

            verify(exactly = 1) {
                runBlocking {
                    deckRepository.findByIdAndUserIdWithStatus(ObjectId(initialDeck.id!!), ObjectId(initialDeck.userId))
                }
            }

            verify(exactly = 0) {
                runBlocking { deckRepository.save(any()) }
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
                runBlocking {
                    deckRepository.existsByIdAndUserIdWithStatus(
                        ObjectId(initialDeck.id),
                        ObjectId(initialDeck.userId),
                        DocumentStatus.ACTIVE,
                    )
                }
            } returns true

            mockFindByIdAndUserIdWithStatus(initialDeck.id!!, initialDeck.userId, initialDeck.toMongo())

            // when
            val actualDeck =
                runBlocking {
                    deckService.updateDeck(updatedDeck)
                }

            // then
            actualDeck shouldBe initialDeck

            verify(exactly = 1) {
                runBlocking {
                    deckRepository.existsByIdAndUserIdWithStatus(
                        ObjectId(initialDeck.id),
                        ObjectId(initialDeck.userId),
                        DocumentStatus.ACTIVE,
                    )
                }
            }

            verify(exactly = 1) {
                runBlocking {
                    deckRepository.findByIdAndUserIdWithStatus(ObjectId(initialDeck.id!!), ObjectId(initialDeck.userId))
                }
            }

            verify(exactly = 0) {
                runBlocking {
                    deckRepository.save(any())
                }
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
                runBlocking {
                    deckRepository.existsByIdAndUserIdWithStatus(
                        ObjectId(initialDeck.id),
                        ObjectId(initialDeck.userId),
                        DocumentStatus.ACTIVE,
                    )
                }
            } returns true
            mockFindByIdAndUserIdWithStatus(initialDeck.id!!, initialDeck.userId, initialDeck.toMongo())

            every {
                runBlocking {
                    deckRepository.save(any())
                }
            } returns expectedDeck.toMongo()

            // when
            val actualDeck = runBlocking { deckService.updateDeck(updatedDeck) }

            // then
            actualDeck shouldBe expectedDeck

            verify(exactly = 1) {
                runBlocking {
                    deckRepository.existsByIdAndUserIdWithStatus(
                        ObjectId(initialDeck.id),
                        ObjectId(initialDeck.userId),
                        DocumentStatus.ACTIVE,
                    )
                }
            }

            verify(exactly = 1) {
                runBlocking {
                    deckRepository.findByIdAndUserIdWithStatus(ObjectId(initialDeck.id!!), ObjectId(initialDeck.userId))
                }
            }

            verify(exactly = 1) {
                runBlocking {
                    deckRepository.save(any())
                }
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
                runBlocking {
                    deckRepository.existsByIdAndUserIdWithStatus(
                        ObjectId(initialDeck.id),
                        ObjectId(initialDeck.userId),
                        DocumentStatus.ACTIVE,
                    )
                }
            } returns true

            mockFindByIdAndUserIdWithStatus(initialDeck.id!!, initialDeck.userId, initialDeck.toMongo())

            every {
                runBlocking {
                    deckRepository.save(any())
                }
            } returns expectedDeck.toMongo()

            // when
            val actualDeck = runBlocking { deckService.updateDeck(updatedDeck) }

            // then
            actualDeck shouldBe expectedDeck

            verify(exactly = 1) {
                runBlocking {
                    deckRepository.existsByIdAndUserIdWithStatus(
                        ObjectId(initialDeck.id),
                        ObjectId(initialDeck.userId),
                        DocumentStatus.ACTIVE,
                    )
                }
            }

            verify(exactly = 1) {
                runBlocking {
                    deckRepository.findByIdAndUserIdWithStatus(ObjectId(initialDeck.id!!), ObjectId(initialDeck.userId))
                }
            }

            verify(exactly = 1) {
                runBlocking {
                    deckRepository.save(any())
                }
            }
        }
        private fun mockFindByIdAndUserIdWithStatus(deckId: String, userId: String, mongoDeck: MongoDeck) {
            every {
                runBlocking {
                    deckRepository.findByIdAndUserIdWithStatus(ObjectId(deckId), ObjectId(userId))
                }
            } returns mongoDeck
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
                runBlocking {
                    deckRepository.existsByIdAndUserIdWithStatus(
                        deckId,
                        userId,
                        DocumentStatus.ACTIVE,
                    )
                }
            } returns true

            every {
                runBlocking {
                    deckRepository.softDelete(deckId)
                }
            } returns Unit
            every {
                runBlocking {
                    cardRepository.softDeleteByDeckId(deckId)
                }
            } returns Unit

            // when
            runBlocking {
                deckService.deleteDeck(deckId.toString(), userId.toString())
            }

            // then

            verify(exactly = 1) {
                runBlocking {
                    deckRepository.existsByIdAndUserIdWithStatus(
                        deckId,
                        userId,
                        DocumentStatus.ACTIVE,
                    )
                }
            }
            verify(exactly = 1) {
                runBlocking {
                    deckRepository.softDelete(deckId)
                }
            }
            verify(exactly = 1) {
                runBlocking {
                    cardRepository.softDeleteByDeckId(deckId)
                }
            }
        }
    }
}
