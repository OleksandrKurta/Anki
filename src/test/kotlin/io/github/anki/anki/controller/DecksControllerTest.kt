package io.github.anki.anki.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.anki.anki.controller.dto.DeckDtoResponse
import io.github.anki.anki.controller.dto.NewDeckRequest
import io.github.anki.anki.controller.dto.PatchDeckRequest
import io.github.anki.anki.controller.dto.mapper.toDeck
import io.github.anki.anki.controller.dto.mapper.toDto
import io.github.anki.anki.repository.mongodb.CardRepository
import io.github.anki.anki.repository.mongodb.DeckRepository
import io.github.anki.anki.repository.mongodb.document.DocumentStatus
import io.github.anki.anki.service.exceptions.DeckDoesNotExistException
import io.github.anki.anki.service.model.mapper.toDeck
import io.github.anki.anki.service.model.mapper.toMongo
import io.github.anki.testing.MVCTest
import io.github.anki.testing.getRandomID
import io.github.anki.testing.getRandomString
import io.github.anki.testing.insertRandom
import io.github.anki.testing.testcontainers.TestContainersFactory
import io.github.anki.testing.testcontainers.with
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.bson.types.ObjectId
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
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
import kotlin.test.Test

@MVCTest
class DecksControllerTest @Autowired constructor(
    val mockMvc: MockMvc,
    val objectMapper: ObjectMapper,
    val deckRepository: DeckRepository,
    val cardRepository: CardRepository,
) {
    private val baseUrl = "/api/v1/decks"
    private val mockUserId = "66a11305dc669eefd22b5f3a"
    private lateinit var newDeckRequest: NewDeckRequest

    @BeforeTest
    fun setUp() {
        newDeckRequest =
            NewDeckRequest(
                name = getRandomString("initial"),
                description = getRandomString("initial"),
            )
    }

    @Nested
    @DisplayName("POST /api/v1/decks")
    @TestInstance(Lifecycle.PER_CLASS)
    inner class PostDeck {
        @Test
        fun `should create new Deck`() =
            runBlocking {
                // when
                val performPost = postNewDeck(newDeckRequest)

                val createdDeck =
                    performPost.andReturn()
                        .response
                        .contentAsString
                        .let { objectMapper.readValue(it, DeckDtoResponse::class.java) }

                // then
                performPost
                    .andDo { print() }
                    .andExpect {
                        status { isCreated() }
                        content { contentType(MediaType.APPLICATION_JSON) }
                    }

                createdDeck.name shouldBe newDeckRequest.name
                createdDeck.description shouldBe newDeckRequest.description

                val deckFromMongo = deckRepository.findById(ObjectId(createdDeck.id))!!

                createdDeck shouldBe deckFromMongo.toDeck().toDto()
            }

        @ParameterizedTest
        @MethodSource("invalidNewDeckRequestProvider")
        fun `should be error if deck name is not valid`(nameValue: String?) =
            runBlocking {
                // given
                newDeckRequest =
                    NewDeckRequest(
                        name = nameValue,
                        description = getRandomString(),
                    )

                // when
                val performPost = postNewDeck(newDeckRequest)

                // then
                performPost
                    .andDo { print() }
                    .andExpect {
                        status { isBadRequest() }
                        content {
                            contentType(MediaType.APPLICATION_JSON)
                            json("{\"name\": \"must not be blank\"}")
                        }
                    }
            }

        private fun postNewDeck(newDeck: NewDeckRequest): ResultActionsDsl =
            mockMvc.post(baseUrl) {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(newDeck)
            }.asyncDispatch()

        @Suppress("UnusedPrivateMember")
        private fun invalidNewDeckRequestProvider(): Stream<Arguments> =
            Stream.of(
                Arguments.of(null),
                Arguments.of(""),
            )
    }

    @Nested
    @DisplayName("GET api/v1/decks/")
    @TestInstance(Lifecycle.PER_CLASS)
    inner class GetDecks {
        @Test
        fun `should return all decks if they exist`() {
            runBlocking {
                // given
                val numberOfRandomCards = (5..100).random()
                val insertedDecks = deckRepository.insertRandom(numberOfRandomCards, ObjectId(mockUserId))

                // when
                val performGet = sendGetDecks()

                // then
                val result =
                    performGet
                        .andExpect {
                            status { isOk() }
                            content { contentType(MediaType.APPLICATION_JSON) }
                        }
                        .andReturn()

                val actualDecks: List<DeckDtoResponse> = objectMapper.readValue(result.response.contentAsString)

                actualDecks shouldContainExactlyInAnyOrder insertedDecks.map { it.toDeck().toDto() }
            }
        }

        private fun sendGetDecks(): ResultActionsDsl =
            mockMvc.get(baseUrl).asyncDispatch()
    }

    @Nested
    @DisplayName("PATCH api/v1/decks/{deckId}")
    @TestInstance(Lifecycle.PER_CLASS)
    inner class PatchDeck {
        @Test
        fun `should patch deck if it exists`() =
            runBlocking {
                // given
                val insertedDeck = deckRepository.insertRandom(1, ObjectId(mockUserId)).first()

                val patchDeckRequest =
                    PatchDeckRequest(
                        name = getRandomString("updated"),
                        description = getRandomString("updated"),
                    )

                // when
                val patchDeckResponse =
                    sendPatchDeck(insertedDeck.id.toString(), patchDeckRequest)
                        .andExpect {
                            status { isOk() }
                            content { contentType(MediaType.APPLICATION_JSON) }
                        }
                        .andReturn()

                val actualDeck: DeckDtoResponse = objectMapper.readValue(patchDeckResponse.response.contentAsString)

                // then
                actualDeck.name shouldBe patchDeckRequest.name
                actualDeck.description shouldBe patchDeckRequest.description

                val deckFromMongo = deckRepository.findById(insertedDeck.id!!)!!

                deckFromMongo.name shouldBe patchDeckRequest.name

                deckFromMongo.description shouldBe patchDeckRequest.description
            }

        @Test
        fun `should return 400 if it does not exist`() =
            runBlocking {
                // given
                val notExistingDeckID = getRandomID().toString()

                // when
                val performPatch = sendPatchDeck(notExistingDeckID, PatchDeckRequest())

                // then
                val result =
                    performPatch
                        .andExpect {
                            status { isBadRequest() }
                        }
                        .andReturn()

                result.response.contentAsString shouldBe
                    DeckDoesNotExistException.fromDeckIdAndUserId(notExistingDeckID, mockUserId).message

                deckRepository.existsById(ObjectId(notExistingDeckID)) shouldBe false
            }

        private fun sendPatchDeck(deckId: String, patchDeckRequest: PatchDeckRequest): ResultActionsDsl =
            mockMvc.patch("$baseUrl/$deckId") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(patchDeckRequest)
            }.asyncDispatch()
    }

    @Nested
    @DisplayName("DELETE api/v1/decks/{id}")
    @TestInstance(Lifecycle.PER_CLASS)
    inner class DeleteDeck {
        @Test
        fun `should delete the deck`() {
            runBlocking {
                // given
                val insertedDeck = deckRepository.insert(newDeckRequest.toDeck(mockUserId).toMongo())
                val insertedCards = cardRepository.insertRandom((5..100).random(), insertedDeck.id!!)

                // when
                val performDelete = sendDeleteDeck(insertedDeck.id!!.toString())
                // then
                val result =
                    performDelete
                        .andExpect {
                            status { isNoContent() }
                        }
                        .andReturn()

                result.response.contentType shouldBe null

                result.response.contentAsString.isEmpty() shouldBe true

                val existsWithStatusActive =
                    async {
                        deckRepository.existsByIdWithStatus(insertedDeck.id!!, DocumentStatus.ACTIVE)
                    }

                val findWithStatusDeleted =
                    async {
                        cardRepository.findByDeckIdWithStatus(insertedDeck.id!!, DocumentStatus.DELETED)
                    }

                existsWithStatusActive.await() shouldBe false
                findWithStatusDeleted.await().size shouldBe insertedCards.size
            }
        }

        private fun sendDeleteDeck(deckId: String): ResultActionsDsl =
            mockMvc.delete("$baseUrl/$deckId").asyncDispatch()
    }

    companion object {
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
