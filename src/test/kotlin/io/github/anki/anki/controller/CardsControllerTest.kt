package io.github.anki.anki.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.anki.anki.controller.dto.CardDtoResponse
import io.github.anki.anki.controller.dto.NewCardRequest
import io.github.anki.anki.controller.dto.PatchCardRequest
import io.github.anki.anki.controller.dto.mapper.toDto
import io.github.anki.anki.repository.mongodb.CardRepository
import io.github.anki.anki.repository.mongodb.DeckRepository
import io.github.anki.anki.repository.mongodb.document.MongoCard
import io.github.anki.anki.repository.mongodb.document.MongoDeck
import io.github.anki.anki.service.model.mapper.toCard
import io.github.anki.testing.MVCTest
import io.github.anki.testing.getRandomID
import io.github.anki.testing.getRandomString
import io.github.anki.testing.insertRandomCards
import io.github.anki.testing.insertRandomDecks
import io.github.anki.testing.testcontainers.TestContainersFactory
import io.github.anki.testing.testcontainers.with
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.bson.types.ObjectId
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActionsDsl
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.post
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Container
import java.util.stream.Stream
import kotlin.test.BeforeTest
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@MVCTest
class CardsControllerTest @Autowired constructor(
    val mockMvc: MockMvc,
    val objectMapper: ObjectMapper,
    val cardRepository: CardRepository,
    val deckRepository: DeckRepository,
) {
    private val baseUrl = "/api/v1/decks/%s/cards"
    private val mockUserId = "66a11305dc669eefd22b5f3a"
    private lateinit var newCard: NewCardRequest
    private lateinit var insertedDeck: MongoDeck

    @BeforeTest
    fun setUp() {
        newCard =
            NewCardRequest(
                cardKey = getRandomString(),
                cardValue = getRandomString(),
            )
        insertedDeck = insertRandomDecks(deckRepository, 1, userId = ObjectId(mockUserId)).first()
        LOG.info("Inserted new Deck {}", insertedDeck)
    }

    @Nested
    @DisplayName("POST /api/v1/decks/{deckId}/cards")
    @TestInstance(Lifecycle.PER_CLASS)
    inner class PostCard {
        @Test
        fun `should create new Card`() {
            val performPost = postNewCard(newCard, insertedDeck.id!!.toString())

            val createdCard =
                performPost.andReturn()
                    .response
                    .contentAsString
                    .let { objectMapper.readValue(it, CardDtoResponse::class.java) }

            performPost
                .andDo { print() }
                .andExpect {
                    status { isCreated() }
                    content {
                        contentType(MediaType.APPLICATION_JSON)
                        json(objectMapper.writeValueAsString(createdCard))
                    }
                }

            val cardFromMongo = cardRepository.findById(ObjectId(createdCard.id))

            cardFromMongo shouldNotBe null

            createdCard shouldBe cardFromMongo!!.toCard().toDto()
        }

        @Test
        fun `should return 400 if deck does not exist`() {
            // when
            val performPost = postNewCard(newCard, getRandomID().toString())
            val result =
                performPost
                    .andDo { print() }
                    .andExpect { status { isBadRequest() } }
                    .andReturn()
            // then

            result.response.contentAsString shouldBe "Deck does not exist"
        }

        @ParameterizedTest
        @MethodSource("invalidNewRequestCardsProvider")
        fun `should return 400 if any value is not valid`(fieldName: String, newCard: NewCardRequest) {
            // when
            val performPost = postNewCard(newCard, insertedDeck.id!!.toString())

            // then
            performPost
                .andDo { print() }
                .andExpect {
                    status { isBadRequest() }
                    content {
                        contentType(MediaType.APPLICATION_JSON)
                        json("{\"$fieldName\": \"should not be blank\"}")
                    }
                }
        }

        private fun postNewCard(newCard: NewCardRequest, deckId: String): ResultActionsDsl =
            mockMvc.post(String.format(baseUrl, deckId)) {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(newCard)
            }

        @Suppress("UnusedPrivateMember")
        private fun invalidNewRequestCardsProvider(): Stream<Arguments> {
            return Stream.of(
                Arguments.of("cardKey", NewCardRequest(cardKey = null, cardValue = getRandomString())),
                Arguments.of("cardKey", NewCardRequest(cardKey = "", cardValue = getRandomString())),
                Arguments.of("cardValue", NewCardRequest(cardKey = getRandomString(), cardValue = null)),
                Arguments.of("cardValue", NewCardRequest(cardKey = getRandomString(), cardValue = "")),
            )
        }
    }

    @Nested
    @DisplayName("GET api/v1/decks/{deckId}/cards")
    @TestInstance(Lifecycle.PER_CLASS)
    inner class GetCards {
        @Test
        fun `should return all cards if they exist`() {
            // given
            val mongoCards = insertRandomCards(cardRepository, (5..100).random(), insertedDeck.id!!)

            // when
            val result = sendGetCards(insertedDeck.id!!.toString())

            // then
            result.andExpect {
                status { isOk() }
            }

            val cardsFromResponse: List<CardDtoResponse> =
                result
                    .andReturn()
                    .let { objectMapper.readValue(it.response.contentAsString) }

            cardsFromResponse shouldBe mongoCards.map { it.toCard().toDto() }
        }

        @Test
        fun `should return empty list if there are no cards`() {
            // when
            val result = sendGetCards(insertedDeck.id!!.toString())

            // then
            result.andExpect {
                status { isOk() }
            }

            val cardsFromResponse: List<CardDtoResponse> =
                result
                    .andReturn()
                    .let { objectMapper.readValue(it.response.contentAsString) }

            cardsFromResponse.isEmpty() shouldBe true
        }

        private fun sendGetCards(deckId: String): ResultActionsDsl = mockMvc.get(String.format(baseUrl, deckId))
    }

    @Nested
    @DisplayName("PATCH api/v1/decks/{deckId}/cards/{id}")
    @TestInstance(Lifecycle.PER_CLASS)
    inner class PatchCard {
        private val patchBaseUrl = "/api/v1/decks/%s/cards/%s"
        private lateinit var insertedCard: MongoCard

        @BeforeTest
        fun createCard() {
            insertedCard = insertRandomCards(cardRepository, 1, insertedDeck.id!!).first()
            LOG.info("Inserted new Card {}", insertedCard)
        }

        @Test
        fun `should patch card`() {
            // given

            val patchCardRequest = PatchCardRequest(cardKey = getRandomString(), cardValue = getRandomString())

            // when
            val actualCard =
                sendPatchCardAndValidateStatusAndContentType(
                    insertedDeck.id.toString(),
                    insertedCard.id.toString(),
                    patchCardRequest,
                )

            // then
            actualCard.cardKey shouldBe patchCardRequest.cardKey
            actualCard.cardValue shouldBe patchCardRequest.cardValue

            val cardFromMongo = cardRepository.findById(insertedCard.id!!)!!

            cardFromMongo.cardKey shouldBe patchCardRequest.cardKey

            cardFromMongo.cardValue shouldBe patchCardRequest.cardValue
        }

        @Test
        fun `should change nothing if all fields is null`() {
            // given
            val patchCardRequest = PatchCardRequest(cardKey = null, cardValue = null)

            // when
            val actualCard =
                sendPatchCardAndValidateStatusAndContentType(
                    insertedDeck.id.toString(),
                    insertedCard.id.toString(),
                    patchCardRequest,
                )

            // then
            actualCard shouldBe insertedCard.toCard().toDto()

            val cardFromMongo = cardRepository.findById(insertedCard.id!!)!!

            cardFromMongo shouldBe insertedCard
        }

        @Test
        fun `should change nothing if all fields is actual`() {
            // given
            val patchCardRequest = PatchCardRequest(cardKey = insertedCard.cardKey, cardValue = insertedCard.cardValue)

            // when
            val actualCard =
                sendPatchCardAndValidateStatusAndContentType(
                    insertedDeck.id.toString(),
                    insertedCard.id.toString(),
                    patchCardRequest,
                )

            // then
            actualCard shouldBe insertedCard.toCard().toDto()

            val cardFromMongo = cardRepository.findById(insertedCard.id!!)!!

            cardFromMongo shouldBe insertedCard
        }

        @Test
        fun `should patch only cardKey if card exists`() {
            // given
            val patchCardRequest = PatchCardRequest(cardKey = getRandomString())

            // when
            val actualCard =
                sendPatchCardAndValidateStatusAndContentType(
                    insertedDeck.id.toString(),
                    insertedCard.id.toString(),
                    patchCardRequest,
                )

            // then
            actualCard.cardKey shouldBe patchCardRequest.cardKey
            actualCard.cardValue shouldBe insertedCard.cardValue

            val cardFromMongo = cardRepository.findById(insertedCard.id!!)!!

            cardFromMongo.cardKey shouldBe patchCardRequest.cardKey

            cardFromMongo.cardValue shouldBe insertedCard.cardValue
        }

        @Test
        fun `should patch only cardValue if card exists`() {
            // given
            val patchCardRequest = PatchCardRequest(cardValue = getRandomString())

            // when
            val actualCard =
                sendPatchCardAndValidateStatusAndContentType(
                    insertedDeck.id.toString(),
                    insertedCard.id.toString(),
                    patchCardRequest,
                )

            // then
            actualCard.cardKey shouldBe insertedCard.cardKey
            actualCard.cardValue shouldBe patchCardRequest.cardValue

            val cardFromMongo = cardRepository.findById(insertedCard.id!!)!!

            cardFromMongo.cardKey shouldBe insertedCard.cardKey

            cardFromMongo.cardValue shouldBe patchCardRequest.cardValue
        }

        @Test
        fun `should return 400 if deck does not exist`() {
            // when
            val performPatch =
                sendPatchCard(
                    getRandomID().toString(),
                    cardId = insertedCard.id.toString(),
                    PatchCardRequest(),
                )

            // then
            val result =
                performPatch
                    .andExpect {
                        status { isBadRequest() }
                    }
                    .andReturn()

            result.response.contentAsString shouldBe "Deck does not exist"
        }

        @Test
        fun `should return 400 if card does not exist`() {
            // when
            val performPatch =
                sendPatchCard(
                    insertedDeck.id.toString(),
                    cardId = getRandomID().toString(),
                    PatchCardRequest(),
                )

            // then
            val result =
                performPatch
                    .andExpect {
                        status { isBadRequest() }
                    }
                    .andReturn()

            result.response.contentAsString shouldBe "Card does not exist"
        }

        private fun sendPatchCardAndValidateStatusAndContentType(
            deckId: String,
            cardId: String,
            patchCardRequest: PatchCardRequest,
        ): CardDtoResponse =
            sendPatchCard(deckId, cardId, patchCardRequest)
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                }
                .andReturn()
                .let { objectMapper.readValue(it.response.contentAsString) }

        private fun sendPatchCard(
            deckId: String,
            cardId: String,
            patchCardRequest: PatchCardRequest,
        ): ResultActionsDsl =
            mockMvc.patch(String.format(patchBaseUrl, deckId, cardId)) {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(patchCardRequest)
            }
    }

    @Nested
    @DisplayName("DELETE api/v1/decks/{deckId}/cards/{id}")
    @TestInstance(Lifecycle.PER_CLASS)
    inner class DeleteCard {
        private val deleteBaseUrl = "/api/v1/decks/%s/cards/%s"

        @Test
        fun `should delete the card`() {
            // given
            val model = insertRandomCards(cardRepository, 1, deckId = insertedDeck.id!!).first()

            // when
            val performDelete = sendDeleteCard(insertedDeck.id!!.toString(), model.id!!.toString())
            // then
            val result =
                performDelete
                    .andExpect {
                        status { isNoContent() }
                    }
                    .andReturn()

            assertNull(result.response.contentType)

            assertTrue(result.response.contentAsString.isEmpty())

            assertFalse(cardRepository.existsById(model.id!!))
        }

        @Test
        fun `should get 204 when no card exists`() {
            // given
            val notExistingCardID = ObjectId.get()

            // when
            val performDelete = sendDeleteCard(insertedDeck.id!!.toString(), notExistingCardID.toString())

            // when/then
            val result =
                performDelete
                    .andDo { print() }
                    .andExpect { status { isNoContent() } }
                    .andReturn()

            assertNull(result.response.contentType)

            assertTrue(result.response.contentAsString.isEmpty())

            assertFalse(cardRepository.existsById(notExistingCardID))
        }

        @Test
        fun `should get 400 when deck does not exists`() {
            // given
            val notExistingCardID = ObjectId.get()

            // when
            val performDelete = sendDeleteCard(getRandomID().toString(), notExistingCardID.toString())

            // when/then
            val result =
                performDelete
                    .andDo { print() }
                    .andExpect { status { isBadRequest() } }
                    .andReturn()

            result.response.contentAsString shouldBe "Deck does not exist"
        }

        private fun sendDeleteCard(deckId: String, cardId: String): ResultActionsDsl =
            mockMvc.delete(String.format(deleteBaseUrl, deckId, cardId))
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(CardsControllerTest::class.java)

        @Container
        @Suppress("PropertyName")
        private val mongoDBContainer: MongoDBContainer = TestContainersFactory.newMongoContainer()

        @DynamicPropertySource
        @JvmStatic
        fun setMongoUri(registry: DynamicPropertyRegistry) {
            registry.with(mongoDBContainer)
        }
    }
}
