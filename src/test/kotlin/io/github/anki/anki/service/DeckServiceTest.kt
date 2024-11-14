package io.github.anki.anki.service

import io.github.anki.anki.repository.mongodb.CardRepository
import io.github.anki.anki.repository.mongodb.DeckRepository
import io.github.anki.anki.repository.mongodb.document.DocumentStatus
import io.github.anki.anki.repository.mongodb.document.MongoDeck
import io.github.anki.anki.service.exceptions.DeckDoesNotExistException
import io.github.anki.anki.service.model.Deck
import io.github.anki.anki.service.model.mapper.toDeck
import io.github.anki.anki.service.model.mapper.toMongo
import io.github.anki.anki.service.utils.toObjectId
import io.github.anki.testing.DATA_PREFIX
import io.github.anki.testing.getRandomID
import io.github.anki.testing.getRandomString
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
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.util.stream.Stream
import kotlin.test.Test

@ExtendWith(MockKExtension::class)
class DeckServiceTest {

    @MockK
    private lateinit var deckRepository: DeckRepository

    @MockK
    private lateinit var cardRepository: CardRepository

    @InjectMockKs
    private lateinit var deckService: DeckService

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
                    userId = userId.toString(),
                    name = "name",
                    description = "description",
                )
            val mongoDeck = deck.toMongo()
            val createdMongoDeck = mongoDeck.copy(id = getRandomID())
            val expectedDeck = deck.copy(id = createdMongoDeck.id!!.toString())
            every {
                deckRepository.insert(mongoDeck)
            } returns Mono.just(createdMongoDeck)

            // when/then
            StepVerifier
                .create(
                    deckService.createNewDeck(deck),
                )
                .expectNext(expectedDeck)
                .verifyComplete()
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
            val expectedDecks = randomDecks.map { it.toDeck() }

            every {
                deckRepository.findByUserIdWithStatus(userId)
            } returns Flux.fromIterable(randomDecks)

            // when/then
            StepVerifier
                .create(
                    deckService
                        .getDecks(userId.toString())
                        .collectList(),
                )
                .assertNext {
                    it.size shouldBe numberOfDecks
                    it shouldContainExactlyInAnyOrder expectedDecks
                }
                .verifyComplete()
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

        @ParameterizedTest(name = "{3}")
        @MethodSource("getArgumentsForUpdateDeckTest")
        fun `should update deck if it exists`(
            initialDeck: Deck,
            updatedDeck: Deck,
            expectedDeck: Deck,
            @Suppress("UNUSED_PARAMETER")
            testName: String,
        ) {
            // given
            every {
                deckRepository.existsByIdAndUserIdWithStatus(
                    initialDeck.id!!.toObjectId(),
                    initialDeck.userId.toObjectId(),
                    DocumentStatus.ACTIVE,
                )
            } returns Mono.just(true)

            every {
                deckRepository
                    .findByIdWithStatus(initialDeck.id!!.toObjectId(), DocumentStatus.ACTIVE)
            } returns Mono.just(initialDeck.toMongo())

            every {
                deckRepository.save(expectedDeck.toMongo())
            } returns Mono.just(expectedDeck.toMongo())

            // when
            StepVerifier
                .create(
                    deckService.updateDeck(updatedDeck),
                )
                .expectNext(expectedDeck)
                .verifyComplete()

            verify(exactly = 1) {
                deckRepository.existsByIdAndUserIdWithStatus(
                    initialDeck.id!!.toObjectId(),
                    initialDeck.userId.toObjectId(),
                    DocumentStatus.ACTIVE,
                )
            }

            verify(exactly = 1) {
                deckRepository.findByIdWithStatus(initialDeck.id!!.toObjectId(), DocumentStatus.ACTIVE)
            }

            verify(exactly = 1) {
                deckRepository.save(expectedDeck.toMongo())
            }
        }

        @Test
        fun `should be error if deck was not found`() {
            // given
            val deckToUpdate =
                Deck(
                    id = getRandomID().toString(),
                    userId = getRandomID().toString(),
                    name = getRandomString(),
                    description = getRandomString(),
                )

            every {
                deckRepository.existsByIdAndUserIdWithStatus(
                    any(),
                    any(),
                    DocumentStatus.ACTIVE,
                )
            } returns Mono.just(false)

            // when/then
            StepVerifier
                .create(
                    deckService.updateDeck(deckToUpdate),
                )
                .verifyError(DeckDoesNotExistException::class.java)
        }

        @Test
        fun `should be error if deckId is null`() {
            // given
            val deckToUpdate =
                Deck(
                    id = null,
                    userId = getRandomID().toString(),
                    name = getRandomString(),
                    description = getRandomString(),
                )
            // when/then
            StepVerifier
                .create(
                    deckService.updateDeck(deckToUpdate),
                )
                .verifyError(IllegalArgumentException::class.java)
        }

        @ParameterizedTest(name = "{2}")
        @MethodSource("getArgumentsForNothingChangeTest")
        fun `should change nothing`(
            initialDeck: Deck,
            updatedDeck: Deck,
            @Suppress("UNUSED_PARAMETER")
            testName: String,
        ) {
            // given
            every {
                deckRepository.existsByIdAndUserIdWithStatus(
                    initialDeck.id!!.toObjectId(),
                    initialDeck.userId.toObjectId(),
                    DocumentStatus.ACTIVE,
                )
            } returns Mono.just(true)

            every {
                deckRepository.findByIdWithStatus(initialDeck.id!!.toObjectId(), DocumentStatus.ACTIVE)
            } returns Mono.just(initialDeck.toMongo())

            // when/then
            StepVerifier
                .create(
                    deckService.updateDeck(updatedDeck),
                )
                .expectNext(initialDeck)
                .verifyComplete()

            verify(exactly = 1) {
                deckRepository.existsByIdAndUserIdWithStatus(
                    initialDeck.id!!.toObjectId(),
                    initialDeck.userId.toObjectId(),
                    DocumentStatus.ACTIVE,
                )
            }

            verify(exactly = 1) {
                deckRepository.findByIdWithStatus(initialDeck.id!!.toObjectId(), DocumentStatus.ACTIVE)
            }

            verify(exactly = 0) {
                deckRepository.save(any())
            }
        }

        @Suppress("UnusedPrivateMember")
        private fun getArgumentsForUpdateDeckTest(): Stream<Arguments> {
            val initialDeck =
                Deck(
                    id = getRandomID().toString(),
                    userId = getRandomID().toString(),
                    name = getRandomString(DATA_PREFIX),
                    description = getRandomString(DATA_PREFIX),
                )
            val randomDeckName = getRandomString("updated")
            val randomDeckDescription = getRandomString("updated")
            return Stream.of(
                Arguments.of(
                    initialDeck,
                    initialDeck.copy(
                        name = randomDeckName,
                        description = randomDeckDescription,
                    ),
                    initialDeck.copy(
                        name = randomDeckName,
                        description = randomDeckDescription,
                    ),
                    "name and description",
                ),
                Arguments.of(
                    initialDeck,
                    initialDeck.copy(name = randomDeckName, description = null),
                    initialDeck.copy(name = randomDeckName, description = initialDeck.description),
                    "only name",
                ),
                Arguments.of(
                    initialDeck,
                    initialDeck.copy(name = null, description = randomDeckDescription),
                    initialDeck.copy(name = initialDeck.name, description = randomDeckDescription),
                    "only description",
                ),
            )
        }

        @Suppress("UnusedPrivateMember")
        private fun getArgumentsForNothingChangeTest(): Stream<Arguments> {
            val initialDeck =
                Deck(
                    id = getRandomID().toString(),
                    userId = getRandomID().toString(),
                    name = getRandomString(DATA_PREFIX),
                    description = getRandomString(DATA_PREFIX),
                )
            return Stream.of(
                Arguments.of(
                    initialDeck,
                    initialDeck,
                    "all fields is initial",
                ),
                Arguments.of(
                    initialDeck,
                    initialDeck.copy(name = null, description = null),
                    "all fields is null",
                ),
            )
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
            } returns Mono.just(true)

            every { deckRepository.softDelete(deckId) } returns Mono.empty()
            every { cardRepository.softDeleteByDeckId(deckId) } returns Mono.empty()

            // when/then
            StepVerifier
                .create(
                    deckService.deleteDeck(deckId.toString(), userId.toString()),
                )
                .verifyComplete()

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
