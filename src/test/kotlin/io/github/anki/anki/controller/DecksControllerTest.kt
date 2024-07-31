package io.github.anki.anki.controller

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.anki.anki.controller.dto.DeckDtoResponse
import io.github.anki.anki.controller.dto.NewDeckRequest
import io.github.anki.anki.controller.dto.mapper.toDeck
import io.github.anki.anki.repository.mongodb.DeckRepository
import io.github.anki.anki.service.model.mapper.toMongo
import io.github.anki.testing.MVCTest
import io.github.anki.testing.getRandomID
import io.github.anki.testing.getRandomString
import io.github.anki.testing.testcontainers.TestContainersFactory
import io.github.anki.testing.testcontainers.with
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
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
import kotlin.test.BeforeTest
import kotlin.test.Test

@MVCTest
class DecksControllerTest @Autowired constructor(
    val mockMvc: MockMvc,
    val objectMapper: ObjectMapper,
    val deckRepository: DeckRepository,
) {

    private val baseUrl = ("/api/v1/decks")
    private val commonUserId = "66a11305dc669eefd22b5f3a"
    private lateinit var newDeck: NewDeckRequest

    @BeforeTest
    fun setUp() {
        LOG.info("Initializing NewDeckRequest")
        newDeck = generateRandomDeck()
    }

    fun generateRandomDeck(): NewDeckRequest =
        NewDeckRequest(
            name = getRandomString(),
            description = getRandomString(),
        )

    @Nested
    @DisplayName("POST /api/v1/decks")
    @TestInstance(Lifecycle.PER_CLASS)
    inner class PostDeck {

        @Test
        fun `should create new Deck`() {
            //when
            val performPost = postNewDeck(newDeck)

            val createdDeck = performPost.andReturn()
                .response
                .contentAsString
                .let { objectMapper.readValue(it, DeckDtoResponse::class.java) }

            //then
            performPost
                .andDo { print() }
                .andExpect {
                    status { isCreated() }
                    content { contentType(MediaType.APPLICATION_JSON)
                        json(objectMapper.writeValueAsString(createdDeck))
                    }
                }
        }

        @ParameterizedTest
        @MethodSource("invalidNewDeckRequestProvider")
        fun `should be error if deck name is not valid`(nameValue: String?) {
            //given
            newDeck = NewDeckRequest(
                name = nameValue,
                description = getRandomString(),
            )

            //when
            val performPost = postNewDeck(newDeck)

            //then
            performPost
                .andDo { print() }
                .andExpect {
                    status { isBadRequest() }
                    content {
                        contentType(MediaType.APPLICATION_JSON)
                        json("{\"name\": \"should not be blank\"}")
                    }
                }
        }

        private fun postNewDeck(newDeck: NewDeckRequest): ResultActionsDsl =
            mockMvc.post(baseUrl) {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(newDeck)
            }

        @Suppress("UnusedPrivateMember")
        private fun invalidNewDeckRequestProvider(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(null),
                Arguments.of(""),
            )
        }
    }

    @Nested
    @DisplayName("DELETE api/v1/decks/{id}")
    @TestInstance(Lifecycle.PER_CLASS)
    inner class DeleteDeck {

        @Test
        fun `should delete the deck`() {
            // given
            val model = deckRepository.insert(newDeck.toDeck(commonUserId).toMongo())

            //when
            val performDelete = sendDeleteDeck(model.id!!.toString())
            // then
            val result = performDelete
                .andExpect {
                    status { (isNoContent()) }
                }.andReturn()

            kotlin.test.assertNull(result.response.contentType)

            kotlin.test.assertTrue(result.response.contentAsString.isEmpty())

            kotlin.test.assertFalse(deckRepository.existsById(model.id!!))
        }

        @Test
        fun `should get IsNoContent when no card exists`() {
            // given
            val notExistingDeckID = getRandomID()

            // when
            val performDelete = sendDeleteDeck(notExistingDeckID.toString())

            // when/then
            val result = performDelete
                .andDo { print() }
                .andExpect { status { isNoContent() } }
                .andReturn()

            kotlin.test.assertNull(result.response.contentType)

            kotlin.test.assertTrue(result.response.contentAsString.isEmpty())

            kotlin.test.assertFalse(deckRepository.existsById(notExistingDeckID))
        }

        private fun sendDeleteDeck(deckId: String): ResultActionsDsl =
            mockMvc.delete("$baseUrl/$deckId")
    }

    companion object {

        private val LOG = LoggerFactory.getLogger(DecksControllerTest::class.java)

        @Container
        private val mongoDBContainer: MongoDBContainer = TestContainersFactory.newMongoContainer()

        @DynamicPropertySource
        @JvmStatic
        fun setMongoUri(registry: DynamicPropertyRegistry) {
            registry.with(mongoDBContainer)
        }

    }
}
