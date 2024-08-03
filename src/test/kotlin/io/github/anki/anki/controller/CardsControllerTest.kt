package io.github.anki.anki.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.anki.anki.controller.dto.CardDtoResponse
import io.github.anki.anki.controller.dto.NewCardRequest
import io.github.anki.anki.controller.dto.mapper.toDto
import io.github.anki.anki.repository.mongodb.CardRepository
import io.github.anki.anki.repository.mongodb.DeckRepository
import io.github.anki.anki.repository.mongodb.document.MongoCard
import io.github.anki.anki.repository.mongodb.document.MongoDeck
import io.github.anki.anki.service.model.mapper.toCard
import io.github.anki.testing.MVCTest
import io.github.anki.testing.testcontainers.with
import io.github.anki.testing.getRandomID
import io.github.anki.testing.getRandomString
import io.github.anki.testing.testcontainers.TestContainersFactory
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

    private val baseUrl = ("/api/v1/decks/%s/cards")
    private val mockUserId = "66a11305dc669eefd22b5f3a"
    private lateinit var newCard: NewCardRequest
    private lateinit var mongoDeck: MongoDeck

    @BeforeTest
    fun setUp() {
        LOG.info("Initializing new card request")
        newCard = generateRandomCard()
        mongoDeck = deckRepository.insert(MongoDeck(
            userId = ObjectId(mockUserId),
            name = getRandomString(),
            description = getRandomString(),
        ))
    }

    fun generateRandomCard(): NewCardRequest =
        NewCardRequest(
            cardKey = getRandomString(),
            cardValue =getRandomString(),
            )

    @Nested
    @DisplayName("POST /api/v1/decks/{deckId}/cards")
    @TestInstance(Lifecycle.PER_CLASS)
    inner class PostCard {

        @Test
        fun `should create new Card`() {
            val performPost = postNewCard(newCard, mongoDeck.id!!.toString())

            val createdCard = performPost.andReturn()
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

            createdCard.id shouldNotBe null
        }

        @Test
        fun `should return error if deck does not exist`() {
            //when
            val performPost = postNewCard(newCard, getRandomID().toString())
            val result = performPost
                .andDo { print() }
                .andExpect { status { isBadRequest() } }
                .andReturn()
            //then

            result.response.contentAsString shouldBe "Deck does not exist"
        }

        @ParameterizedTest
        @MethodSource("invalidNewRequestCardsProvider")
        fun `should be error if any value is not valid`(fieldName: String, newCard: NewCardRequest) {
            //when
            val performPost = postNewCard(newCard, mongoDeck.id!!.toString())

            //then
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
            //given
            val mongoCards = insertRandomCards((5..100).random(), mongoDeck.id!!)

            //when
            val result = sendGetCards(mongoDeck.id!!.toString())

            //then
            result.andExpect {
                status { isOk() }
            }

            val cardsFromResponse: List<CardDtoResponse> = result
                .andReturn()
                .let { objectMapper.readValue(it.response.contentAsString) }

            cardsFromResponse shouldBe mongoCards.map { it.toCard().toDto() }
        }
        private fun sendGetCards(deckId: String): ResultActionsDsl = mockMvc.get(String.format(baseUrl, deckId))
    }

    @Nested
    @DisplayName("DELETE api/v1/decks/{deckId}/cards/{id}")
    @TestInstance(Lifecycle.PER_CLASS)
    inner class DeleteCard {

        @Test
        fun `should delete the card`() {
            // given
            val model = insertRandomCards(1, deckId = mongoDeck.id!!).first()

            //when
            val performDelete = sendDeleteCard(model.id!!.toString())
            // then
            val result = performDelete
                .andExpect {
                    status { (isNoContent()) }
                }.andReturn()

            assertNull(result.response.contentType)

            assertTrue(result.response.contentAsString.isEmpty())

            assertFalse(cardRepository.existsById(model.id!!))
        }

        @Test
        fun `should get IsNoContent when no card exists`() {
            // given
            val notExistingCardID = ObjectId.get()

            // when
            val performDelete = sendDeleteCard(notExistingCardID.toString())

            // when/then
            val result = performDelete
                .andDo { print() }
                .andExpect { status { isNoContent() } }
                .andReturn()

            assertNull(result.response.contentType)

            assertTrue(result.response.contentAsString.isEmpty())

            assertFalse(cardRepository.existsById(notExistingCardID))
        }

        private fun sendDeleteCard(cardId: String): ResultActionsDsl =
            mockMvc.delete("$baseUrl/$cardId")
    }

    private fun insertRandomCards(numberOfCards: Int, deckId: ObjectId): List<MongoCard> {
        val listOfCards: MutableList<MongoCard> = mutableListOf()
        while (listOfCards.size != numberOfCards) {
            listOfCards.add(
                MongoCard(
                    deckId = deckId,
                    cardKey = getRandomString(),
                    cardValue = getRandomString(),
                )
            )
        }
        LOG.info("Inserting {} cards", listOfCards.size)
        return cardRepository.insert(listOfCards)
    }

    companion object {

        private val LOG = LoggerFactory.getLogger(CardsControllerTest::class.java)

        @Container
        private val mongoDBContainer: MongoDBContainer = TestContainersFactory.newMongoContainer()

        @DynamicPropertySource
        @JvmStatic
        fun setMongoUri(registry: DynamicPropertyRegistry) {
            registry.with(mongoDBContainer)
        }

    }
}
