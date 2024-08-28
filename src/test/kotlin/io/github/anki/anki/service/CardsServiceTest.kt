package io.github.anki.anki.service

import io.github.anki.anki.controller.dto.PaginationDto
import io.github.anki.anki.repository.mongodb.CardRepository
import io.github.anki.anki.repository.mongodb.document.MongoCard
import io.github.anki.anki.service.model.Card
import io.github.anki.anki.service.model.mapper.toCard
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
import org.bson.types.ObjectId
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import kotlin.test.Test

@ExtendWith(MockKExtension::class)
class CardsServiceTest {

    @MockK
    lateinit var cardRepository: CardRepository

    @MockK
    lateinit var deckService: DeckService

    @InjectMockKs
    lateinit var cardService: CardsService

    private val mockUserId = getRandomID().toString()

    private lateinit var deckId: ObjectId

    private lateinit var initialMongoCard: MongoCard
    private lateinit var initialCard: Card

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @BeforeEach
    fun baseMockPrecondition() {
        every {
            deckService.validateUserHasPermissions(deckId.toString(), mockUserId)
        } returns Unit
    }

    @BeforeEach
    fun createTestObjects() {
        deckId = ObjectId()
        initialMongoCard = getRandomMongoCards(1, deckId).first()
        initialCard = initialMongoCard.toCard()
    }

    @Nested
    @DisplayName("CardService.createNewCard()")
    @TestInstance(Lifecycle.PER_CLASS)
    inner class CreateNewCard {

        @Test
        fun `should create new card`() {
            // given
            val expectedMongoCard =
                MongoCard(
                    id = getRandomID(),
                    deckId = initialMongoCard.deckId,
                    key = initialCard.key,
                    value = initialCard.value,
                )

            every {
                cardRepository.insert(initialMongoCard)
            } returns expectedMongoCard

            // when
            val actualCard = cardService.createNewCard(mockUserId, initialCard)

            // then
            actualCard shouldBe expectedMongoCard.toCard()

            verify(exactly = 1) {
                cardRepository.insert(initialMongoCard)
            }
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

            val pagination = PaginationDto(limit = cardsAmount)

            every {
                cardRepository.findByDeckIdWithStatus(
                    deckId = deckId,
                    limit = pagination.limit,
                    offset = pagination.offset,
                )
            } returns initialMongoCards

            // when
            val actualDecks =
                cardService.findCardsByDeckWithPagination(
                    deckId.toString(),
                    mockUserId,
                    limit = pagination.limit,
                    offset = pagination.offset,
                )

            // then
            actualDecks.size shouldBe cardsAmount
            actualDecks shouldContainExactlyInAnyOrder initialMongoCards.map { it.toCard() }

            validateValidateUserHasPermissionsWasCalled()
            verify(exactly = 1) {
                cardRepository.findByDeckIdWithStatus(
                    deckId = deckId,
                    limit = pagination.limit,
                    offset = pagination.offset,
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
            every { cardRepository.findById(ObjectId(initialCard.id)) } returns initialMongoCard
        }

        @Test
        fun `should update card`() {
            // given
            val updatedCard =
                Card(
                    id = initialCard.id,
                    deckId = initialCard.deckId,
                    key = getRandomString("updated"),
                    value = getRandomString("updated"),
                )
            val expectedMongoCard =
                MongoCard(
                    id = initialMongoCard.id,
                    deckId = initialMongoCard.deckId,
                    key = updatedCard.key,
                    value = updatedCard.value,
                )

            every { cardRepository.save(updatedCard.toMongo()) } returns expectedMongoCard

            // when
            val actualCard = cardService.updateCard(mockUserId, updatedCard)

            // then
            actualCard shouldBe expectedMongoCard.toCard()

            validateValidateUserHasPermissionsWasCalled()
            baseUpdateValidation()

            verify(exactly = 1) {
                cardRepository.save(updatedCard.toMongo())
            }
        }

        @Test
        fun `should be error if card id is null`() {
            // when/then
            shouldThrowExactly<IllegalArgumentException> {
                cardService.updateCard(
                    mockUserId,
                    Card(
                        id = null,
                        deckId = deckId.toString(),
                        key = null,
                        value = null,
                    ),
                )
            }
        }

        @Test
        fun `should change nothing if all fields is null`() {
            // given
            val updatedCard =
                Card(
                    id = initialCard.id,
                    deckId = deckId.toString(),
                    key = null,
                    value = null,
                )

            // when
            val actualCard = cardService.updateCard(mockUserId, updatedCard)

            // then
            actualCard shouldBe initialCard

            validateValidateUserHasPermissionsWasCalled()
            baseUpdateValidation()

            verify(exactly = 0) {
                cardRepository.save(any())
            }
        }

        @Test
        fun `should change nothing if all fields is actual`() {
            // when
            val actualCard = cardService.updateCard(mockUserId, initialCard)

            // then
            actualCard shouldBe initialCard

            validateValidateUserHasPermissionsWasCalled()
            baseUpdateValidation()

            verify(exactly = 0) {
                cardRepository.save(any())
            }
        }

        @Test
        fun `should update only cardKey`() {
            // given
            val updatedCard =
                Card(
                    id = initialCard.id,
                    deckId = initialCard.deckId,
                    key = getRandomString("updated"),
                    value = null,
                )
            val expectedCard =
                Card(
                    id = initialCard.id,
                    deckId = initialCard.deckId,
                    key = updatedCard.key,
                    value = initialCard.value,
                )

            every { cardRepository.save(expectedCard.toMongo()) } returns expectedCard.toMongo()

            // when
            val actualCard = cardService.updateCard(mockUserId, updatedCard)

            // then
            actualCard shouldBe expectedCard

            validateValidateUserHasPermissionsWasCalled()
            baseUpdateValidation()

            verify(exactly = 1) {
                cardRepository.save(expectedCard.toMongo())
            }
        }

        @Test
        fun `should update only cardValue`() {
            // given
            val updatedCard =
                Card(
                    id = initialCard.id,
                    deckId = initialCard.deckId,
                    key = null,
                    value = getRandomString("updated"),
                )
            val expectedCard =
                Card(
                    id = initialCard.id,
                    deckId = initialCard.deckId,
                    key = initialCard.key,
                    value = updatedCard.value,
                )

            every { cardRepository.save(expectedCard.toMongo()) } returns expectedCard.toMongo()

            // when
            val actualCard = cardService.updateCard(mockUserId, updatedCard)

            // then
            actualCard shouldBe expectedCard

            validateValidateUserHasPermissionsWasCalled()
            baseUpdateValidation()

            verify(exactly = 1) {
                cardRepository.save(expectedCard.toMongo())
            }
        }

        private fun baseUpdateValidation() {
            verify(exactly = 1) {
                cardRepository.findById(initialMongoCard.id!!)
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
            every { cardRepository.softDelete(initialMongoCard.id!!) } returns Unit

            // when
            cardService.deleteCard(initialCard.deckId, mockUserId, initialCard.id!!)

            // then
            validateValidateUserHasPermissionsWasCalled()

            verify(exactly = 1) {
                cardRepository.softDelete(initialMongoCard.id!!)
            }
        }
    }

    private fun validateValidateUserHasPermissionsWasCalled() {
        verify(exactly = 1) {
            deckService.validateUserHasPermissions(initialCard.deckId, mockUserId)
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
