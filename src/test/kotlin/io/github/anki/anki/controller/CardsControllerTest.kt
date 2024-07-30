package io.github.anki.anki.controller

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.anki.anki.controller.dto.CardDtoResponse
import io.github.anki.anki.controller.dto.NewCardRequest
import io.github.anki.anki.controller.dto.mapper.toCard
import io.github.anki.anki.repository.mongodb.CardRepository
import io.github.anki.anki.service.model.mapper.toMongo
import io.github.anki.testing.MVCTest
import io.github.anki.testing.testcontainers.with
import io.github.anki.testing.getRandomID
import io.github.anki.testing.getRandomString
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
import org.springframework.test.web.servlet.post
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Container
import java.util.stream.Stream
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.memberProperties
import kotlin.test.BeforeTest
import kotlin.test.assertNull
import kotlin.test.assertTrue

@MVCTest
class CardsControllerTest @Autowired constructor(
    val mockMvc: MockMvc,
    val objectMapper: ObjectMapper,
    val cardRepository: CardRepository,
) {

    val baseUrl = ("/api/v1/cards")
    private lateinit var newCard: NewCardRequest

    @BeforeTest
    fun setUp() {
        LOG.info("Initializing new card request")
        newCard = generateRandomCard()
    }

    fun generateRandomCard(): NewCardRequest =
        NewCardRequest(
            deckId = getRandomID().toString(),
            cardKey = getRandomString(),
            cardValue =getRandomString(),
            )

    @Nested
    @DisplayName("POST /api/v1/cards")
    @TestInstance(Lifecycle.PER_CLASS)
    inner class PostCards {

        @Test
        fun `should post card`() {
            val performPost = postNewCard(newCard)

            val createdCard = performPost.andReturn()
                .response
                .contentAsString
                .let { objectMapper.readValue(it, CardDtoResponse::class.java) }
            // covered in repository test
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

        @ParameterizedTest
        @MethodSource("invalidNewRequestCardsProvider")
        fun `should be error if any value is null`(fieldName: String, fieldValue: String?) {
            // given
            newCard.setProperty(fieldName, fieldValue)

            //when
            val performPost = postNewCard(newCard)

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

        private fun NewCardRequest.setProperty(propertyName: String, value: Any?) {
            val kClass = NewCardRequest::class
            val property = kClass.memberProperties.find { it.name == propertyName }
                    as? KMutableProperty1<NewCardRequest, *>
                ?: throw IllegalArgumentException("Property $propertyName does not exist")

            property.setter.call(this, value)
        }

        private fun postNewCard(newCard: NewCardRequest): ResultActionsDsl =
            mockMvc.post(baseUrl) {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(newCard)
            }

        @Suppress("UnusedPrivateMember")
        private fun invalidNewRequestCardsProvider(): Stream<Arguments> {
            return Stream.of(
                Arguments.of("deckId", null),
                Arguments.of("deckId", ""),
                Arguments.of("cardKey", null),
                Arguments.of("cardKey", ""),
                Arguments.of("cardValue", null),
                Arguments.of("cardValue", ""),
                )
        }
    }

    @Nested
    @DisplayName("DELETE api/v1/cards/{id}")
    @TestInstance(Lifecycle.PER_CLASS)
    inner class DeleteCard {

        @Test
        fun `should delete the card`() {
            // given
            val model = cardRepository.insert(newCard.toCard().toMongo())

            //when
            val performDelete = sendDeleteCard(model.id!!.toString())
            // then
            val result = performDelete
                .andExpect {
                    status { (isNoContent()) }
                }.andReturn()

            assertNull(result.response.contentType)

            assertTrue(result.response.contentAsString.isEmpty())
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
        }

        private fun sendDeleteCard(cardId: String): ResultActionsDsl =
            mockMvc.delete("$baseUrl/$cardId")
    }

    companion object {

        private val LOG = LoggerFactory.getLogger(CardsControllerTest::class.java)

        @Container
        private val mongoDBContainer: MongoDBContainer = MongoDBContainer("mongo:7")

        @DynamicPropertySource
        @JvmStatic
        fun setMongoUri(registry: DynamicPropertyRegistry) {
            registry.with(mongoDBContainer)
        }

    }
}
