package io.github.anki.anki.service

import io.github.anki.anki.controller.dto.PaginationDto
import io.github.anki.anki.controller.dto.mapper.toPagination
import io.github.anki.anki.repository.mongodb.CardRepository
import io.github.anki.anki.repository.mongodb.document.DocumentStatus
import io.github.anki.anki.repository.mongodb.document.MongoCard
import io.github.anki.anki.service.model.Card
import io.github.anki.anki.service.model.mapper.toCard
import io.github.anki.anki.service.model.mapper.toMongo
import io.github.anki.testing.getRandomID
import io.github.anki.testing.getRandomString
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.bson.types.ObjectId
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
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
class CardsServiceTest {

    @MockK
    private lateinit var cardRepository: CardRepository

    @MockK
    private lateinit var deckService: DeckService

    @InjectMockKs
    private lateinit var cardService: CardsService

    private val mockUserId = getRandomID().toString()

    private val deckId: ObjectId = getRandomID()

    private val initialMongoCard: MongoCard = getRandomMongoCards(1, deckId).first()

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @BeforeEach
    fun baseMockConfig() {
        every {
            deckService.validateUserHasPermissions(any(), any())
        } returns Mono.just(true)
    }

    @Nested
    @DisplayName("CardService.createNewCard()")
    @TestInstance(Lifecycle.PER_CLASS)
    inner class CreateNewCard {

        @Test
        fun `should create new card`() {
            // given
            every {
                cardRepository.insert(initialMongoCard)
            } returns Mono.just(initialMongoCard)

            // when/then
            StepVerifier
                .create(
                    cardService.createNewCard(mockUserId, initialMongoCard.toCard()),
                )
                .expectNext(initialMongoCard.toCard())
                .verifyComplete()

            verify(exactly = 1) {
                cardRepository.insert(initialMongoCard)
            }
            validateValidateUserHasPermissionsWasCalled()
        }
    }

    @Nested
    @DisplayName("CardService.getAllCardsFromDeck")
    @TestInstance(Lifecycle.PER_CLASS)
    inner class GetAllCardsFromDeck {

        @ParameterizedTest
        @ValueSource(ints = [0, 5, 20, 100])
        fun `should return all cards`(cardsAmount: Int) {
            // given
            val initialMongoCards = getRandomMongoCards(cardsAmount, deckId)

            val paginationDto = PaginationDto(limit = cardsAmount)

            every {
                cardRepository.findByDeckIdWithStatus(
                    deckId = deckId,
                    limit = paginationDto.limit,
                    offset = paginationDto.offset,
                )
            } returns Flux.fromIterable(initialMongoCards)

            // when/then
            StepVerifier
                .create(
                    cardService.findCardsByDeckWithPagination(
                        deckId.toString(),
                        mockUserId,
                        paginationDto.toPagination(),
                    )
                        .collectList(),
                )
                .expectNext(initialMongoCards.map { it.toCard() })
                .verifyComplete()

            validateValidateUserHasPermissionsWasCalled()
            verify(exactly = 1) {
                cardRepository.findByDeckIdWithStatus(
                    deckId = deckId,
                    limit = paginationDto.limit,
                    offset = paginationDto.offset,
                )
            }
        }
    }

    @Nested
    @DisplayName("CardsService.updateCard()")
    @TestInstance(Lifecycle.PER_CLASS)
    inner class UpdateCard {

        @BeforeEach
        fun baseUpdatePrecondition() {
            every {
                cardRepository.findByIdWithStatus(initialMongoCard.id!!, DocumentStatus.ACTIVE)
            } returns Mono.just(initialMongoCard)
        }

        @ParameterizedTest(name = "{2}")
        @MethodSource("getArgumentsForUpdateCardTest")
        fun `should update card`(
            updatedCard: Card,
            expectedCard: Card,
            @Suppress("UNUSED_PARAMETER")
            testName: String,
        ) {
            // given
            every {
                cardRepository.save(expectedCard.toMongo())
            } returns Mono.just(expectedCard.toMongo())

            // when/then
            StepVerifier
                .create(
                    cardService.updateCard(mockUserId, updatedCard),
                )
                .expectNext(expectedCard)
                .verifyComplete()

            validateValidateUserHasPermissionsWasCalled()
            baseUpdateValidation()

            verify(exactly = 1) {
                cardRepository.save(expectedCard.toMongo())
            }
        }

        @Test
        fun `should be error if card id is null`() {
            // given
            val cardWithNullId =
                Card(
                    id = null,
                    deckId = deckId.toString(),
                    key = getRandomString(),
                    value = getRandomString(),
                )
            // when/then
            StepVerifier
                .create(
                    cardService.updateCard(mockUserId, cardWithNullId),
                )
                .verifyError(IllegalArgumentException::class.java)
        }

        @ParameterizedTest(name = "{2}")
        @MethodSource("getArgumentsForChangeNothingTest")
        fun `should change nothing if all fields is null`(
            updatedCard: Card,
            expectedCard: Card,
            @Suppress("UNUSED_PARAMETER")
            testName: String,
        ) {
            // when/then
            StepVerifier
                .create(
                    cardService.updateCard(mockUserId, updatedCard),
                )
                .expectNext(expectedCard)
                .verifyComplete()

            validateValidateUserHasPermissionsWasCalled()
            baseUpdateValidation()

            verify(exactly = 0) {
                cardRepository.save(any())
            }
        }

        @Suppress("UnusedPrivateMember")
        private fun getArgumentsForUpdateCardTest(): Stream<Arguments> {
            val initialCard = initialMongoCard.toCard()
            val randomCardKey = getRandomString("updated")
            val randomCardValue = getRandomString("updated")
            return Stream.of(
                Arguments.of(
                    initialCard.copy(key = randomCardKey, value = randomCardValue),
                    initialCard.copy(key = randomCardKey, value = randomCardValue),
                    "key and value",
                ),
                Arguments.of(
                    initialCard.copy(key = randomCardKey, value = null),
                    initialCard.copy(key = randomCardKey, value = initialCard.value),
                    "only key",
                ),
                Arguments.of(
                    initialCard.copy(key = null, value = randomCardValue),
                    initialCard.copy(key = initialCard.key, value = randomCardValue),
                    "only value",
                ),
            )
        }

        @Suppress("UnusedPrivateMember")
        private fun getArgumentsForChangeNothingTest(): Stream<Arguments> {
            val initialCard = initialMongoCard.toCard()
            return Stream.of(
                Arguments.of(
                    initialCard,
                    initialCard,
                    "all fields is initial",
                ),
                Arguments.of(
                    initialCard.copy(key = null, value = null),
                    initialCard,
                    "all fields is null",
                ),
            )
        }

        private fun baseUpdateValidation() {
            verify(exactly = 1) {
                cardRepository.findByIdWithStatus(initialMongoCard.id!!, DocumentStatus.ACTIVE)
            }
        }
    }

    @Nested
    @DisplayName("CardService.deleteCard")
    @TestInstance(Lifecycle.PER_CLASS)
    inner class DeleteCard {

        @Test
        fun `should delete the card`() {
            // given
            every {
                cardRepository.softDelete(initialMongoCard.id!!)
            } returns Mono.empty()

            // when/then
            StepVerifier
                .create(
                    cardService
                        .deleteCard(initialMongoCard.deckId.toString(), mockUserId, initialMongoCard.id!!.toString()),
                )
                .verifyComplete()

            validateValidateUserHasPermissionsWasCalled()

            verify(exactly = 1) {
                cardRepository.softDelete(initialMongoCard.id!!)
            }
        }
    }

    private fun validateValidateUserHasPermissionsWasCalled() {
        verify(exactly = 1) {
            deckService.validateUserHasPermissions(initialMongoCard.deckId.toString(), mockUserId)
        }
    }

    private fun getRandomMongoCards(number: Int, deckId: ObjectId = ObjectId()): List<MongoCard> {
        val mongoCards: MutableCollection<MongoCard> = mutableListOf()
        repeat(number) {
            mongoCards.add(
                MongoCard(
                    id = getRandomID(),
                    deckId = deckId,
                    key = getRandomString("initial"),
                    value = getRandomString("initial"),
                ),
            )
        }
        return mongoCards.toList()
    }
}
