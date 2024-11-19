package io.github.anki.anki.service

import io.github.anki.anki.controller.dto.PaginationDto
import io.github.anki.anki.controller.dto.mapper.toPagination
import io.github.anki.anki.repository.mongodb.CardRepository
import io.github.anki.anki.repository.mongodb.document.DocumentStatus
import io.github.anki.anki.repository.mongodb.document.MongoCard
import io.github.anki.anki.service.model.mapper.toCardEntity
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
import service.model.CardLearningEntity
import service.model.LearnItem
import java.util.concurrent.CompletableFuture
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
    private lateinit var initialCard: CardLearningEntity

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
        initialCard = initialMongoCard.toCardEntity()
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
                    key = initialCard.hint.item.toString(),
                    value = initialCard.answer.item.toString(),
                )

            every {
                cardRepository.insert(initialMongoCard)
            } returns CompletableFuture.completedFuture(expectedMongoCard)

            // when
            val actualCard = cardService.createNewCard(mockUserId, initialCard)

            // then
            actualCard shouldBe expectedMongoCard.toCardEntity()

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

            val paginationDto = PaginationDto(limit = cardsAmount)

            every {
                cardRepository.findByDeckIdWithStatus(
                    deckId = deckId,
                    limit = paginationDto.limit,
                    offset = paginationDto.offset,
                )
            } returns CompletableFuture.completedFuture(initialMongoCards)

            // when
            val actualDecks =
                cardService.findCardsByDeckWithPagination(
                    deckId.toString(),
                    mockUserId,
                    paginationDto.toPagination(),
                )

            // then
            actualDecks.size shouldBe cardsAmount
            actualDecks shouldContainExactlyInAnyOrder initialMongoCards.map { it.toCardEntity() }

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
                cardRepository.findByIdWithStatus(ObjectId(initialCard.id), DocumentStatus.ACTIVE)
            } returns CompletableFuture.completedFuture(initialMongoCard)
        }

        @Test
        fun `should update card`() {
            // given
            val updatedCard =
                CardLearningEntity(
                    initialCard.id,
                    null,
                    initialCard.deckId,
                    LearnItem( getRandomString("updated")),
                    LearnItem(getRandomString("updated")),
                    null
                )
            val expectedMongoCard =
                MongoCard(
                    id = initialMongoCard.id,
                    deckId = initialMongoCard.deckId,
                    key = updatedCard.hint.item.toString(),
                    value = updatedCard.answer.item.toString(),
                )

            every {
                cardRepository.save(updatedCard.toMongo())
            } returns CompletableFuture.completedFuture(expectedMongoCard)

            // when
            val actualCard = cardService.updateCardEntity(mockUserId, updatedCard)

            // then
            actualCard shouldBe expectedMongoCard.toCardEntity()

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
                cardService.updateCardEntity(
                    mockUserId,
                    CardLearningEntity(
                        null,
                        null,
                        deckId.toString(),
                         null,
                       null,
                        null,
                    ),
                )
            }
        }

        @Test
        fun `should change nothing if all fields is null`() {
            // given
            val updatedCard =
                CardLearningEntity(
                    initialCard.id,
                    null,
                    initialCard.deckId,
                    LearnItem( getRandomString("updated")),
                    LearnItem(getRandomString("updated")),
                    null
                )

            // when
            val actualCard = cardService.updateCardEntity(mockUserId, updatedCard)

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
            val actualCard = cardService.updateCardEntity(mockUserId, initialCard)

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
                CardLearningEntity(
                    initialCard.id,
                    null,
                    initialCard.deckId,
                    LearnItem( getRandomString("updated")),
                    LearnItem(null),
                    null
                )
            val expectedCard =
                CardLearningEntity(
                    initialCard.id,
                    null,
                    initialCard.deckId,
                    LearnItem(updatedCard.hint.item.toString()),
                    LearnItem(initialCard.answer.item.toString()),
                    null,
                )

            every {
                cardRepository.save(expectedCard.toMongo())
            } returns CompletableFuture.completedFuture(expectedCard.toMongo())

            // when
            val actualCard = cardService.updateCardEntity(mockUserId, updatedCard)

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
                CardLearningEntity(
                    initialCard.id,
                    null,
                    initialCard.deckId,
                    LearnItem(null),
                    LearnItem(getRandomString("updated")),
                    null,
                )
            val expectedCard =
                CardLearningEntity(
                    initialCard.id,
                    null,
                    initialCard.deckId,
                    LearnItem(initialCard.hint.item.toString()),
                    LearnItem(updatedCard.answer.item.toString()),
                    null,
                )

            every {
                cardRepository.save(expectedCard.toMongo())
            } returns CompletableFuture.completedFuture(expectedCard.toMongo())

            // when
            val actualCard = cardService.updateCardEntity(mockUserId, updatedCard)

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
            } returns CompletableFuture.completedFuture(null)

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
